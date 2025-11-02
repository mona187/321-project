module.exports = {
  // Use ts-jest to handle TypeScript files
  preset: 'ts-jest',
  
  // Set the test environment (node for backend, jsdom for frontend)
  testEnvironment: 'node',
  
  // Where to find test files
  testMatch: [
    '**/__tests__/**/*.test.ts',
    '**/?(*.)+(spec|test).ts'
  ],
  
  // Collect coverage from these files
  collectCoverageFrom: [
    'src/**/*.ts',              // All TypeScript in src
    '!src/**/*.d.ts',           // Exclude type definitions
    '!src/**/*.test.ts',        // Exclude test files
    '!src/**/*.spec.ts',        // Exclude spec files
    '!src/index.ts',            // Exclude main entry point (optional)
    '!src/**/types/**',         // Exclude types folders
    '!src/**/interfaces/**',    // Exclude interfaces folders
  ],
  
  // Coverage thresholds (adjust as needed)
  coverageThreshold: {
    global: {
      branches: 70,
      functions: 70,
      lines: 70,
      statements: 70
    }
  },
  
  // Where to output coverage reports
  coverageDirectory: 'coverage',
  
  // Coverage report formats
  coverageReporters: ['text', 'lcov', 'html'],
  
  // Setup files to run before tests
  setupFilesAfterEnv: ['<rootDir>/tests/setup.ts'],
  
  // Module path aliases (if you use them)
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/src/$1',
  },
  
  // Clear mocks between tests
  clearMocks: true,
  
  // Restore mocks between tests
  restoreMocks: true,
  
  // Verbose output
  verbose: true,

  maxWorkers: 1,  // Run all tests sequentially (one at a time) to prevent test conflicts

};