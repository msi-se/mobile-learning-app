package de.htwg_konstanz.mobilelearning.models.auth;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.models.stats.UserStats;

public class User {
    public ObjectId id;
	public String email;
    public String name;
    public String username;
    public String password;
    public List<String> roles;
    public List<ObjectId> courses;
    public UserStats stats;

    public User() {
    }

    /**
     * Constructor for User.
     * Generates a new ObjectId for the user.
     * 
     * @param email
     * @param name
     * @param username
     * @param password
     */
    public User(String email, String name, String username, String password) {
        this.id = new ObjectId();
        this.email = email;
        this.name = name;
        this.username = username;
        this.password = password;
        this.roles = new ArrayList<String>();
        this.courses = new ArrayList<ObjectId>();

        // check if email, name and username have ": " in it and if so, only take the part after it
        if (this.email.contains(": ")) {
            this.email = this.email.split(": ")[1];
        }
        if (this.name.contains(": ")) {
            this.name = this.name.split(": ")[1];
        }
        if (this.username.contains(": ")) {
            this.username = this.username.split(": ")[1];
        }
        System.out.println("User constructed: " + this.toString());
    }

    public String getEmail() {
        return this.email;
    }

    public String getName() {
        return this.name;
    }

    public String getUsername() {
        return this.username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Assigns roles to user based on ldap id.
     * Ids 121, 103 or 137 are reserved for Profs.
     * 
     * @param id
     */
	public void assignProfAndStudentRoleByLdapId(String id) {
		if (id == null || id.isBlank()){
			return;
        }
		try {
			id = id.split(":")[1].trim();

			if (id != null && (Integer.valueOf(id) == 121) || (Integer.valueOf(id) == 103)
					|| (Integer.valueOf(id) == 137)) {
				this.roles.add(UserRole.PROF);
			} else {
                this.roles.add(UserRole.STUDENT);
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public Boolean checkIsProf() {
        return this.roles != null ? this.roles.contains(UserRole.PROF) : false;
    }

    public Boolean checkIsStudent() {
        return this.roles != null ? this.roles.contains(UserRole.STUDENT) : false;
    }

    public Boolean checkIsAdmin() {
        return this.roles != null ? this.roles.contains(UserRole.ADMIN) : false;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
	public String toString() {
        return "User [id=" + this.id.toHexString() + ", email=" + this.email + ", name=" + this.name + ", username=" + this.username + ", password="
                + this.password + ", roles=" + this.roles + "]";
	}

    public boolean authenticate(String password) {
        return this.password.equals(password);
    }

    public String getPassword() {
        return this.password;
    }

    public ObjectId getId() {
        return this.id;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getRoles() {
        return this.roles;
    }

    public void addRole(String role) {
        this.roles.add(role);
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    // courses
    public List<ObjectId> getCourses() {
        if (this.courses == null) this.courses = new ArrayList<ObjectId>();
        return this.courses;
    }

    public void setCourses(List<ObjectId> courses) {
        this.courses = courses;
    }

    public void addCourse(ObjectId course) {
        if (this.courses == null) this.courses = new ArrayList<ObjectId>();
        if (!this.courses.contains(course)) {
            this.courses.add(course);
        }
    }

    public void removeCourse(ObjectId course) {
        if (this.courses == null) this.courses = new ArrayList<ObjectId>();
        try {
            this.courses.remove(course);
        } catch (Exception e) {
            System.out.println("not in list");
        }
    }

    public boolean hasCourse(ObjectId course) {
        if (this.courses == null) this.courses = new ArrayList<ObjectId>();
        return this.courses.contains(course);
    }

    public boolean hasCourse(String courseId) {
        if (this.courses == null) this.courses = new ArrayList<ObjectId>();
        return this.courses.contains(new ObjectId(courseId));
    }

    public void clearCourses() {
        if (this.courses == null) this.courses = new ArrayList<ObjectId>();
        this.courses.clear();
    }

    public UserStats getStats() {
        return this.stats;
    }

    public void setStats(UserStats stats) {
        this.stats = stats;
    }

}
