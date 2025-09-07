-- Migration script to add new columns to vocabulary table
-- Run this script on your database

ALTER TABLE vocabulary 
ADD COLUMN category VARCHAR(255) AFTER example_sentence,
ADD COLUMN difficulty VARCHAR(50) AFTER category,
ADD COLUMN part_of_speech VARCHAR(50) AFTER difficulty,
ADD COLUMN synonyms TEXT AFTER part_of_speech,
ADD COLUMN antonyms TEXT AFTER synonyms,
ADD COLUMN notes TEXT AFTER antonyms;

-- Add index for better performance on category searches
CREATE INDEX idx_vocabulary_category ON vocabulary(category);
CREATE INDEX idx_vocabulary_difficulty ON vocabulary(difficulty);
CREATE INDEX idx_vocabulary_part_of_speech ON vocabulary(part_of_speech);

-- Optional: Add some sample data for testing
-- UPDATE vocabulary SET difficulty = 'MEDIUM', part_of_speech = 'NOUN' WHERE id = 1;
