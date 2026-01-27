CREATE TABLE IF NOT EXISTS student_hold (
    hold_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id BIGINT NOT NULL,
    hold_type VARCHAR(50) NOT NULL,
    reason TEXT,
    active BOOLEAN DEFAULT TRUE,
    placed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    cleared_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_hold_student FOREIGN KEY (student_id) REFERENCES students(student_id)
);