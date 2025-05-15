# Personal Finance Tracker Application

## Team Members
- ST10279176@rcconnect.edu.za
- ST10364204@rcconnect.edu.za
- ST10263952@rcconnect.edu.za

[Video Demo](https://youtu.be/p4-qlzsbpYc)

## Overview
This Android application is a comprehensive personal finance management tool that helps users track their income, expenses, and overall financial health. The app features a modern Material Design 3 interface with both light and dark theme support.

## Features

### 1. Dashboard
- Real-time balance tracking
- Visual representation of financial data
- Dark/Light theme toggle
- Material Design 3 UI components

### 2. Expense Management
- Add and track expenses
- Categorize expenses
- Date and time tracking
- Detailed expense history
- Input validation

### 3. Income Tracking
- Record income sources
- Categorize income
- Date and time tracking
- Income history

### 4. Financial Overview
- Total balance calculation
- Income vs Expenses comparison
- Transaction history
- Category-wise breakdown

## Technical Implementation

### Architecture
- Built using Kotlin
- Follows Material Design 3 guidelines
- Uses MVVM architecture pattern
- Implements modern Android development practices

### Key Components

#### UI Components
- MaterialCardView for financial cards
- TextInputLayout for form inputs
- RecyclerView for transaction lists
- MaterialToolbar with theme toggle
- CoordinatorLayout for smooth scrolling

#### Data Management
- Local storage using SharedPreferences
- JSON serialization for data persistence
- GSON for data conversion
- File-based storage system

#### Theme Implementation
- Dynamic theme switching
- Material 3 color system
- Custom color palette
- Dark/Light mode support

### Color Scheme
- Primary Colors:
  - Light Theme: Purple (#6750A4)
  - Dark Theme: Light Purple (#BB86FC)
- Secondary Colors:
  - Teal (#80CBC4)
  - Dark Teal (#00796B)
- Accent Colors:
  - Blue (#2196F3)
  - Green (#4CAF50)
  - Orange (#FF9800)
  - Red (#F44336)

## Development Process

### Phase 1: Basic Structure
- Set up project architecture
- Implement basic UI components
- Create data models

### Phase 2: Core Functionality
- Implement expense tracking
- Add income management
- Create balance calculation

### Phase 3: UI Enhancement
- Implement Material Design 3
- Add theme switching
- Enhance visual components

### Phase 4: Data Persistence
- Implement local storage
- Add data serialization
- Create backup system

## Future Enhancements
1. Data visualization with charts
2. Budget planning features
3. Export functionality
4. Cloud synchronization
5. Multiple currency support
6. Receipt scanning
7. Financial goals tracking

## Technical Requirements
- Android Studio Arctic Fox or newer
- Minimum SDK: API 21 (Android 5.0)
- Target SDK: Latest Android version
- Kotlin 1.5.0 or higher
- Material Design 3 components

## Dependencies
- androidx.core:core-ktx
- androidx.appcompat:appcompat
- com.google.android.material:material
- androidx.constraintlayout:constraintlayout
- com.google.code.gson:gson

## Setup Instructions
1. Clone the repository
2. Open project in Android Studio
3. Sync Gradle files
4. Build and run the application

## Contributing
Feel free to contribute to this project by:
1. Forking the repository
2. Creating a feature branch
3. Submitting a pull request

## License
This project is licensed under the MIT License - see the LICENSE file for details. 