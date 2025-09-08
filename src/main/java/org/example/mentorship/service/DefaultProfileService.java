package org.example.mentorship.service;

import org.example.mentorship.dto.MentorInfoDto;
import org.example.mentorship.dto.ProfileDtoRequest;
import org.example.mentorship.dto.StudentInfoDto;
import org.example.mentorship.entity.Mentor;
import org.example.mentorship.entity.Profile;
import org.example.mentorship.entity.Student;
import org.example.mentorship.entity.User;
import org.example.mentorship.repository.MentorRepository;
import org.example.mentorship.repository.ProfileRepository;
import org.example.mentorship.repository.StudentRepository;
import org.example.mentorship.repository.UserRepository;
import org.example.mentorship.security.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import java.io.ByteArrayOutputStream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;


@Service
public class DefaultProfileService implements ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final S3Client s3Client;
    private final EmailVerificationService emailVerificationService;
    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final CurrentUserProvider currentUserProvider;

    private static final Logger logger = LoggerFactory.getLogger(DefaultProfileService.class);

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Autowired
    public DefaultProfileService(ProfileRepository profileRepository,
                                 UserRepository userRepository,
                                 S3Client s3Client,
                                 EmailVerificationService emailVerificationService,
                                 StudentRepository studentRepository,
                                 MentorRepository mentorRepository,
                                 CurrentUserProvider currentUserProvider) {
        this.emailVerificationService = emailVerificationService;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.s3Client = s3Client;
        this.studentRepository = studentRepository;
        this.mentorRepository = mentorRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public void createProfile(ProfileDtoRequest profileDto, StudentInfoDto studentInfo, MentorInfoDto mentorInfo, MultipartFile profilePicture) throws IOException {
        User user = userRepository.findById(profileDto.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (profileRepository.existsByUserId(user.getId())) {
            throw new RuntimeException("Profile already exists for this user");
        }

        String profilePictureUrl = uploadProfilePictureAndReturnUrl(profilePicture, profileDto.userId());

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setFirstName(profileDto.firstName());
        profile.setLastName(profileDto.lastName());
        profile.setBio(profileDto.bio());
        profile.setAge(profileDto.age());
        profile.setProfilePictureUrl(profilePictureUrl);

        profileRepository.save(profile);

        // Determine the user's role and save the corresponding data
        String roleName = user.getRole().getRoleName();

        if ("ROLE_STUDENT".equals(roleName)) {
            if (studentInfo == null) {
                throw new RuntimeException("Student information is required for student role");
            }
            createStudentProfile(user, studentInfo);
        } else if ("ROLE_MENTOR".equals(roleName)) {
            if (mentorInfo == null) {
                throw new RuntimeException("Mentor information is required for mentor role");
            }
            createMentorProfile(user, mentorInfo);
        }

        // Send a verification email after creating the profile
        if (!user.isEmailVerified()) {
            emailVerificationService.createVerificationToken(user);
        }
    }

    private String uploadProfilePictureAndReturnUrl(MultipartFile file, Integer userId) throws IOException {
        try {
            // Compress the image before uploading
            byte[] compressedImageData = compressImage(file);

            // Generate a unique key with hash for security
            String uniqueId = UUID.randomUUID().toString();
            String hash = generateHash(userId + uniqueId);
            String fileName = "profiles/" + userId + "/" + hash + "-" + file.getOriginalFilename();

            // Create PutObjectRequest
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            // Upload the compressed file to S3
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(compressedImageData));

            // Generate the object URL
            GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            return s3Client.utilities().getUrl(getUrlRequest).toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash for file name", e);
        }
    }

    private byte[] compressImage(MultipartFile file) throws IOException {
        // Get the original bytes for size comparison
        byte[] originalBytes = file.getBytes();
        long originalSize = originalBytes.length;

        // Determine the image type
        String contentType = file.getContentType();
        String formatName = contentType != null ? contentType.substring(contentType.lastIndexOf('/') + 1) : "jpeg";

        // Adaptive compression level: stronger compression for larger files
        float compressionQuality = 0.7f; // Base quality level
        if (originalSize > 500_000) { // For files > 500KB
            compressionQuality = 0.6f;
        }
        if (originalSize > 1_000_000) { // For files > 1MB
            compressionQuality = 0.5f;
        }

        // Read the original image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IOException("Failed to read the image");
        }

        // Create an output stream for the compressed image
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Compress based on the format
        if ("jpeg".equalsIgnoreCase(formatName) || "jpg".equalsIgnoreCase(formatName)) {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(compressionQuality);

            ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(originalImage, null, null), param);
            writer.dispose();
            ios.close();
        } else if ("png".equalsIgnoreCase(formatName)) {
            // Use standard compression for PNG
            ImageIO.write(originalImage, "png", outputStream);
        } else {
            // Use standard compression for other formats
            ImageIO.write(originalImage, formatName, outputStream);
        }

        // Get the compressed bytes
        byte[] compressedBytes = outputStream.toByteArray();

        // Compare sizes and return the smaller option
        if (compressedBytes.length < originalBytes.length) {
            System.out.println("Compression reduced size: " + originalSize + " -> " + compressedBytes.length + " bytes");
            return compressedBytes;
        } else {
            System.out.println("Compression did not improve size, using original: " + originalSize + " bytes");
            return originalBytes;
        }
    }

    private String generateHash(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 16);
    }

    private void createStudentProfile(User user, StudentInfoDto studentInfo) {
        if (studentRepository.existsByUserId(user.getId())) {
            throw new RuntimeException("Student profile already exists for this user");
        }

        Student student = new Student();
        student.setUser(user);
        student.setEducationLevel(studentInfo.educationLevel());
        student.setLearningGoals(studentInfo.learningGoals());

        studentRepository.save(student);
    }

    private void createMentorProfile(User user, MentorInfoDto mentorInfo) {
        if (mentorRepository.existsByUserId(user.getId())) {
            throw new RuntimeException("Mentor profile already exists for this user");
        }

        Mentor mentor = new Mentor();
        mentor.setUser(user);
        mentor.setHourlyRate(mentorInfo.hourlyRate());
        mentor.setSpecialization(mentorInfo.specialization());
        mentor.setExperienceYears(mentorInfo.experienceYears());
        mentor.setMentorTargetStudents(mentorInfo.mentorTargetStudents());
        mentor.setAverageRating(BigDecimal.ZERO);

        mentorRepository.save(mentor);
    }

    private void deleteOldProfilePictureFromS3(String oldProfilePictureUrl) {
        try {
            URI uri = URI.create(oldProfilePictureUrl);
            String path = uri.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            String decodedKey = URLDecoder.decode(path, StandardCharsets.UTF_8);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(decodedKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid URL for the old profile picture", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete the old profile picture from S3", e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateProfilePicture(MultipartFile profilePicture) {
        return currentUserProvider.withCurrentUser(user -> {
            try {
                // Validate input
                if (profilePicture == null || profilePicture.isEmpty()) {
                    return ResponseEntity.badRequest().body("Profile picture file is required");
                }

                Profile profile = profileRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new RuntimeException("Profile not found"));

                String oldProfilePictureUrl = profile.getProfilePictureUrl();

                // Upload new
                String newProfilePictureUrl = uploadProfilePictureAndReturnUrl(profilePicture, user.getId());

                // Update and save
                profile.setProfilePictureUrl(newProfilePictureUrl);
                profileRepository.save(profile);

                // Delete old only if exists and different from new (prevent deleting overwritten)
                if (oldProfilePictureUrl != null && !oldProfilePictureUrl.isEmpty() && !oldProfilePictureUrl.equals(newProfilePictureUrl)) {
                    deleteOldProfilePictureFromS3(oldProfilePictureUrl);
                } else {
                    logger.info("No need to delete old picture: {}", oldProfilePictureUrl == null ? "null" : "same as new");
                }

                return ResponseEntity.ok(Map.of(
                        "profilePictureUrl", newProfilePictureUrl,
                        "message", "Profile picture updated successfully"
                ));
            } catch (RuntimeException e) {
                logger.error("Error updating profile picture: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error updating profile picture: " + e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}