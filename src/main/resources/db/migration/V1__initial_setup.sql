--GRANT USAGE ON SCHEMA public TO public;
--GRANT CREATE ON SCHEMA public TO public;


CREATE TABLE IF NOT EXISTS courses (
    course_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    course_name VARCHAR(255) NOT NULL,
    course_code VARCHAR(10) UNIQUE NOT NULL,
    credit_hours INTEGER NOT NULL,
    --prerequisites TEXT, deleted in favor of a separate prerequisites table
    semesters_taught TEXT,
    program TEXT,
    department VARCHAR(100),
    description TEXT,
    repeatable BOOLEAN DEFAULT FALSE,
    placement_exam_available BOOLEAN DEFAULT FALSE,
    general_education_course VARCHAR(100)
    );

CREATE TABLE IF NOT EXISTS academic_program (
    program_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    degree_type VARCHAR(100),
    school VARCHAR(100),
    is_minor BOOLEAN DEFAULT FALSE,
    required_gpa DECIMAL(3, 2) DEFAULT 2.0,
    description TEXT
    );


CREATE TABLE IF NOT EXISTS students (
    student_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    status VARCHAR(100) DEFAULT 'ACTIVE',
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(100),
    address VARCHAR(250),
    gpa NUMERIC(3, 2) CHECK (gpa >= 0 AND gpa <= 4.0),
    credits_completed INTEGER DEFAULT 0,
    credits_attempted INTEGER DEFAULT 0,
    major_id BIGINT,
    minor_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_major FOREIGN KEY (major_id) REFERENCES academic_program(program_id),
    CONSTRAINT fk_minor FOREIGN KEY (minor_id) REFERENCES academic_program(program_id)
    );

CREATE TABLE IF NOT EXISTS course_offering(
    offering_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    course_id BIGINT NOT NULL,
    semester VARCHAR(50) NOT NULL,
    instructor VARCHAR(100),
    start_time TIME,
    end_time TIME,
    days_taught VARCHAR(10),
    date_range VARCHAR(50),
    delivery VARCHAR(50),
    location VARCHAR(100),
    seat_capacity INTEGER,
    enrolled INTEGER DEFAULT 0,
    section SMALLINT,
    CONSTRAINT fk_course FOREIGN KEY (course_id) REFERENCES courses(course_id)
    );

CREATE TABLE IF NOT EXISTS wishlist (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id BIGINT NOT NULL,
    offering_id BIGINT NOT NULL,
    added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wishlist_student FOREIGN KEY (student_id) REFERENCES students(student_id),
    CONSTRAINT fk_wishlist_offering FOREIGN KEY (offering_id) REFERENCES course_offering(offering_id)
    );

-- requirement_id BIGINT REFERENCES requirements(requirement_id), TODO: we need this to potentially link to a requirements table in the future

CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id BIGINT NOT NULL,
    offering_id BIGINT NOT NULL,
    enrolled_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    dropped_at TIMESTAMP WITH TIME ZONE,
    grade VARCHAR(2),
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    enrollment_status VARCHAR(20),
    waitlist_position INT,
    credits_attempted INT DEFAULT 0,
    CONSTRAINT fk_enroll_student FOREIGN KEY (student_id) REFERENCES students(student_id),
    CONSTRAINT fk_enroll_offering FOREIGN KEY (offering_id) REFERENCES course_offering(offering_id),
    UNIQUE (student_id, offering_id)
    );

CREATE TABLE IF NOT EXISTS course_prerequisites (
    prerequisite_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    course_id BIGINT NOT NULL,

    --(prereqs with same groupid are OR'd together
    group_id BIGINT NOT NULL,

    --type of requirement: course, min_grade, standing
    prerequisite_type VARCHAR(20) NOT NULL,

    required_course_id BIGINT,
    min_grade_value DECIMAL(3, 2),
    required_standing VARCHAR(20),

    CONSTRAINT fk_main_course FOREIGN KEY (course_id) REFERENCES courses(course_id),
    CONSTRAINT fk_required_course FOREIGN KEY (required_course_id) REFERENCES courses(course_id),

    CONSTRAINT check_grade_range CHECK (min_grade_value IS NULL OR (min_grade_value >= 0.0 AND min_grade_value <= 4.0))
    );

CREATE TABLE IF NOT EXISTS grade_weights (
    grade_letter VARCHAR(2) PRIMARY KEY,
    weight DECIMAL(3, 2) NOT NULL CHECK (weight >= 0.0 AND weight <= 4.0)
    );

INSERT INTO grade_weights (grade_letter, weight) VALUES
('A', 4.0),
('A-', 3.7),
('B+', 3.3),
('B', 3.0),
('B-', 2.7),
('C+', 2.3),
('C', 2.0),
('C-', 1.7),
('D+', 1.3),
('D', 1.0),
('D-', 0.7),
('F', 0.0);

CREATE INDEX IF NOT EXISTS idx_course_prereq_course ON course_prerequisites(course_id);

CREATE TABLE IF NOT EXISTS major_requirements (
    major_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    PRIMARY KEY (major_id, course_id),
    CONSTRAINT fk_major_req FOREIGN KEY (major_id) REFERENCES academic_program(program_id),
    CONSTRAINT fk_course_req FOREIGN KEY (course_id) REFERENCES courses(course_id)
    );

