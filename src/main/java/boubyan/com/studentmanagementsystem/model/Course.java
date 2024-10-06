package boubyan.com.studentmanagementsystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
@SqlResultSetMapping(
        name = "CourseWithStudentsMapping",
        entities = {
                @EntityResult(entityClass = Course.class),
                @EntityResult(entityClass = UserProfile.class)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "course_student",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<UserProfile> students = new HashSet<>();

    public void addStudent(UserProfile student) {
        students.add(student);
        student.getCourses().add(this);
    }

    public void removeStudent(UserProfile student) {
        students.remove(student);
        student.getCourses().remove(this);
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", students=" + students +
                '}';
    }
}