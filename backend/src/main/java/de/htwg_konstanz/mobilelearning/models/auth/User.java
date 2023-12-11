package de.htwg_konstanz.mobilelearning.models.auth;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

public class User {
    public ObjectId id;
	public String email;
    public String name;
    public String username;
    public String password;
    public List<String> roles;

    public User() {
    }

    public User(String email, String name, String username, String password) {
        this.id = new ObjectId();
        this.email = email;
        this.name = name;
        this.username = username;
        this.password = password;
        this.roles = new ArrayList<String>();

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
        return this.roles.contains(UserRole.PROF);
    }

    public Boolean checkIsStudent() {
        return this.roles.contains(UserRole.STUDENT);
    }

    public Boolean checkIsAdmin() {
        return this.roles.contains(UserRole.ADMIN);
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

}
