# Database

## JPA Entity

- Every field requires `@Column(name = "...", nullable = ...)`
- Field name must match DB column name exactly (snake_case)
- `@Table(name = "...")` required on every entity class

```java
@Entity
@Table(name = "chat_room")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "is_group", nullable = false)
    private boolean isGroup;

    @Column(name = "mentoring_id", nullable = true)
    private Long mentoringId;
}
```

## Flyway

- Location: `src/main/resources/db/migration/`
- Naming: `V{VERSION}__{DESCRIPTION}.sql` (double underscore)
- NEVER modify a deployed migration — always create a new version
