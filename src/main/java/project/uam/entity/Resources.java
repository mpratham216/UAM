package project.uam.entity;

public class Resources {
	private int id;
    private String name;
    private String description;
    private boolean managerOnly;
    //getters and setters
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	public boolean isManagerOnly() {
		return managerOnly;
	}
	public void setManagerOnly(boolean managerOnly) {
		this.managerOnly = managerOnly;
	}
	@Override
    public String toString() {
        return "Resource{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
    
}
