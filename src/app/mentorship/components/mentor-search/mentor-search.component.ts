// src/app/mentorship/components/mentor-search/mentor-search.component.ts
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MentorshipService } from '../../services/mentorship.service';
import { MentorSearchRequest } from '../../models/mentor-search.model';
import { MentorAllDataDto } from '../../models/mentor.model';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { ErrorModalComponent } from '../../../shared/components/error-modal/error-modal.component';

@Component({
  selector: 'app-mentor-search',
  templateUrl: './mentor-search.component.html',
  styleUrls: ['./mentor-search.component.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ErrorModalComponent]
})
export class MentorSearchComponent implements OnInit {
  @Output() searchResults = new EventEmitter<MentorAllDataDto[]>();
  @Output() isSearching = new EventEmitter<boolean>();

  searchForm!: FormGroup;
  specializations: string[] = [
    'JavaScript', 'Python', 'Java', 'C#', 'Ruby', 'PHP',
    'Swift', 'Kotlin', 'Go', 'Rust', 'Frontend', 'Backend',
    'Fullstack', 'DevOps', 'Mobile Development', 'Data Science', // Changed from 'Мобильная разработка' to 'Mobile Development'
    'Machine Learning', 'Testing', 'UI/UX Design' // Changed from 'Тестирование' to 'Testing' and 'UI/UX дизайн' to 'UI/UX Design'
  ];
  errorMessage = '';
  showErrorModal = false;
  sortOptions = [
    { value: 'averageRating,DESC', label: 'Rating: Descending' }, // Changed from 'Рейтинг: по убыванию'
    { value: 'averageRating,ASC', label: 'Rating: Ascending' }, // Changed from 'Рейтинг: по возрастанию'
    { value: 'hourlyRate,ASC', label: 'Price: Low to High' }, // Changed from 'Цена: от низкой к высокой'
    { value: 'hourlyRate,DESC', label: 'Price: High to Low' }, // Changed from 'Цена: от высокой к низкой'
    { value: 'experienceYears,DESC', label: 'Experience: Descending' }, // Changed from 'Опыт: по убыванию'
    { value: 'experienceYears,ASC', label: 'Experience: Ascending' } // Changed from 'Опыт: по возрастанию'
  ];

  constructor(
    private fb: FormBuilder,
    private mentorshipService: MentorshipService
  ) {}

  ngOnInit(): void {
    this.initSearchForm();
    this.setupFormListeners();
    // Initial search when the component loads
    this.performSearch();
  }

  initSearchForm(): void {
    this.searchForm = this.fb.group({
      keyword: [''],
      specialization: [''],
      minRating: [null],
      maxRating: [null],
      minHourlyRate: [null],
      maxHourlyRate: [null],
      minExperienceYears: [null],
      maxExperienceYears: [null],
      sort: ['rating,desc']
    });
  }

  setupFormListeners(): void {
    this.searchForm.valueChanges
      .pipe(
        debounceTime(500),
        distinctUntilChanged((prev, curr) => JSON.stringify(prev) === JSON.stringify(curr))
      )
      .subscribe(() => {
        this.performSearch();
      });
  }

  performSearch(): void {
    const formValues = this.searchForm.value;
    const searchRequest: MentorSearchRequest = {
      query: formValues.keyword || undefined,       // Changed from keyword to query
      specialization: formValues.specialization || undefined,
      minRating: formValues.minRating || undefined,
      maxRating: formValues.maxRating || undefined,
      minRate: formValues.minHourlyRate || undefined,   // Changed to minRate
      maxRate: formValues.maxHourlyRate || undefined,   // Changed to maxRate
      minExperience: formValues.minExperienceYears || undefined, // Changed to minExperience
      maxExperience: formValues.maxExperienceYears || undefined
    };

    // Handle sorting
    if (formValues.sort) {
      const [sortBy, sortDirection] = formValues.sort.split(',');
      searchRequest.sortBy = sortBy === 'rating' ? 'averageRating' : sortBy; // Convert rating to averageRating
      searchRequest.sortDirection = sortDirection.toUpperCase() as 'ASC' | 'DESC'; // Convert to uppercase
    }

    this.isSearching.emit(true);
    this.mentorshipService.searchMentors(searchRequest).subscribe({
      next: (results) => {
        this.searchResults.emit(results);
        this.isSearching.emit(false);
      },
      error: (error) => {
        console.error('Error while searching for mentors:', error); // Changed from 'Ошибка при поиске менторов'
        this.errorMessage = 'Failed to perform mentor search'; // Changed from 'Не удалось выполнить поиск менторов'
        this.showErrorModal = true;
        this.isSearching.emit(false);
      }
    });
  }

  resetSearch(): void {
    this.searchForm.reset({
      sort: 'averageRating,DESC'  // Update default value
    });
    this.performSearch();
  }

  closeErrorModal(): void {
    this.showErrorModal = false;
    this.errorMessage = '';
  }
}
