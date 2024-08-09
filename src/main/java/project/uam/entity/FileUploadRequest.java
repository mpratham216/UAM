package project.uam.entity;

public class FileUploadRequest {
	public String fileName;
    public String base64Content;
    
    public FileUploadRequest() {}
    
    

	public FileUploadRequest(String fileName, String base64Content) {
		this.fileName = fileName;
		this.base64Content = base64Content;
	}



	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getBase64Content() {
		return base64Content;
	}

	public void setBase64Content(String base64Content) {
		this.base64Content = base64Content;
	}
    
	
	
    
    
}
