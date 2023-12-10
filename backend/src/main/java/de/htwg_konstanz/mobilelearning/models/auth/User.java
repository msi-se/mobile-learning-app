package de.htwg_konstanz.mobilelearning.models.auth;

import org.bson.types.ObjectId;

public class User {
    private ObjectId id;
	private String email;
	private boolean isTeacher;
    private String name;
    private String username;

    public User() {
    }

    public User(String email, String name, String username) {
        this.id = new ObjectId();
        this.email = email;
        this.isTeacher = false;
        this.name = name;
        this.username = username;
        try {
			this.email = email.split(": ")[1];
			this.name = name.split(": ")[1];
            this.username = username.split(": ")[1];
        } catch (Exception e) {
			;
		}
    }

    public String getEmail() {
        return this.email;
    }

    public boolean getIsTeacher() {
        return this.isTeacher;
    }

    public String getName() {
        return this.name;
    }

    public String getUsername() {
        return this.username;
    }

    public void setEmail(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

	public void setTeacher(String id) {
		if (id == null || id.isBlank()){
			return;
        }
		try {
			id = id.split(":")[1].trim();

			if (id != null && (Integer.valueOf(id) == 121) || (Integer.valueOf(id) == 103)
					|| (Integer.valueOf(id) == 137)) {
				isTeacher = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Override
	public String toString() {
		return (name + "; " + email + "; isProf=" + isTeacher +" " + username);
	}
}
