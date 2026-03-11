package repositories;

import com.mysql.cj.log.Log;
import records.User;
import utilities.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class UserRepository {

    private final DataSource dataSource;

    private static final String SELECT_ALL = """
        SELECT id, username, first_name, last_name, email, password_hash,
               phone_number, date_of_birth, gender, profile_picture_url,
               country, city, street_address, postal_code,
               accepted_terms, accepted_privacy_policy, is_verified,
               created_at, updated_at
        FROM users
    """;

    private static final String SELECT_LOGIN_USER_INFO_ONLY = """
        SELECT id, username, password_hash, is_verified
        FROM users
    """;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ── save new user ─────────────────────────────────────────
    public User save(User user) throws SQLException {
        String sql = """
            INSERT INTO users (
                username, first_name, last_name, email, password_hash,
                phone_number, date_of_birth, gender, profile_picture_url,
                country, city, street_address, postal_code,
                accepted_terms, accepted_privacy_policy,
                is_verified, created_at, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE, NOW(), NOW())
            """; // no RETURNING * — MySQL doesn't support it

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1,  user.username());
            ps.setString(2,  user.firstName());
            ps.setString(3,  user.lastName());
            ps.setString(4,  user.email());
            ps.setString(5,  user.passwordHash());
            ps.setString(6,  user.phoneNumber());
            ps.setObject(7,  user.dateOfBirth());
            ps.setString(8,  user.gender());
            ps.setString(9,  user.profilePictureUrl());
            ps.setString(10, user.country());
            ps.setString(11, user.city());
            ps.setString(12, user.streetAddress());
            ps.setString(13, user.postalCode());
            ps.setBoolean(14, user.acceptedTerms());
            ps.setBoolean(15, user.acceptedPrivacyPolicy());

            // executeUpdate() for INSERT/UPDATE/DELETE
            ps.executeUpdate();

            // get the auto-generated id from MySQL
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long generatedId = keys.getLong(1);
                    Logger.info("Registered user: "+generatedId);
                    return findById(generatedId)
                            .orElseThrow(() -> new SQLException("Failed to fetch saved user"));
                }
            }
        }
        throw new SQLException("Failed to save user");
    }

    // ── find by email ─────────────────────────────────────────
    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = SELECT_ALL + "WHERE email = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Logger.info("findByEmail -> " + Optional.of(mapRow(rs)));
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }



    // ── check email duplicate only ────────────────────────────
    public boolean isDuplicateEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ── check username duplicate only ─────────────────────────
    public boolean isDuplicateUsername(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ── find which field is duplicate — email or username ─────
    public Optional<String> findDuplicate(String email, String username) throws SQLException {
        String sql = """
        SELECT
            CASE
                WHEN email    = ? THEN 'email'
                WHEN username = ? THEN 'username'
            END AS duplicate
        FROM users
        WHERE (? IS NOT NULL AND email = ?)
           OR (? IS NOT NULL AND username = ?)
        LIMIT 1
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, username);
            ps.setString(3, email);
            ps.setString(4, email);
            ps.setString(5, username);
            ps.setString(6, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getString("duplicate"));
            }
        }
        return Optional.empty();
    }

    public Optional<String> getPasswordHashByEmail(String email) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE email = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()){
                    Logger.info("Returned password-hash for email: "+email+ "["+Optional.of(rs.getString("password_hash"))+"]");
                    return Optional.of(rs.getString("password_hash"));

                }
            }
        }
        return Optional.empty();
    }

    // ── find by id ────────────────────────────────────────────
    public Optional<User> findById(long id) throws SQLException {
        String sql = SELECT_ALL + "WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    // ── find by username ──────────────────────────────────────
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = SELECT_ALL + "WHERE username = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    // ── check email exists ────────────────────────────────────
    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ── check username exists ─────────────────────────────────
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ── check email AND username in one query ─────────────────
    // avoids two round trips to DB
    public boolean existsByEmailOrUsername(String email, String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ? OR username = ? LIMIT 1";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ── verify user email ─────────────────────────────────────
    public void verifyUser(long id) throws SQLException {
        String sql = """
            UPDATE users
            SET is_verified = TRUE,
                updated_at  = NOW()
            WHERE id = ?
              AND is_verified = FALSE  -- no-op if already verified
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    // ── update password ───────────────────────────────────────
    public void updatePassword(long id, String newPasswordHash) throws SQLException {
        String sql = """
            UPDATE users
            SET password_hash = ?,
                updated_at    = NOW()
            WHERE id = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPasswordHash);
            ps.setLong(2, id);

            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("records.User not found for password update");
        }
    }

    // ── update profile ────────────────────────────────────────
    public User updateProfile(long id, String firstName, String lastName,
                              String phoneNumber, String gender,
                              String profilePictureUrl) throws SQLException {
        String sql = """
            UPDATE users
            SET first_name          = ?,
                last_name           = ?,
                phone_number        = ?,
                gender              = ?,
                profile_picture_url = ?,
                updated_at          = NOW()
            WHERE id = ?
            RETURNING *
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, phoneNumber);
            ps.setString(4, gender);
            ps.setString(5, profilePictureUrl);
            ps.setLong(6, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        throw new SQLException("records.User not found for profile update");
    }

    // ── soft delete — marks deleted, doesn't remove row ───────
    // requires adding deleted_at TIMESTAMP column to table
    public void softDelete(long id) throws SQLException {
        String sql = """
            UPDATE users
            SET deleted_at = NOW(),
                updated_at = NOW()
            WHERE id = ?
              AND deleted_at IS NULL  -- already deleted guard
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    // ── hard delete ───────────────────────────────────────────
    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("records.User not found for deletion");
        }
    }

    // ── map ResultSet → records.User ──────────────────────────────────
    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("phone_number"),
                rs.getDate("date_of_birth")   != null ? rs.getDate("date_of_birth").toLocalDate()             : null,
                rs.getString("gender"),
                rs.getString("profile_picture_url"),
                rs.getString("country"),
                rs.getString("city"),
                rs.getString("street_address"),
                rs.getString("postal_code"),
                rs.getBoolean("accepted_terms"),
                rs.getBoolean("accepted_privacy_policy"),
                rs.getBoolean("is_verified"),
                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null
        );
    }
}