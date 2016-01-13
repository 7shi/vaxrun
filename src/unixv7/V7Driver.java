package unixv7;

public class V7Driver {
	
	private V7Extractor extractor;
	
	public V7Driver() {
		extractor = new V7Extractor();
		extractor.process("rp06.disk");
	}
	
	public V7Driver(String disk) {
		extractor = new V7Extractor();
		extractor.process(disk);
	}
	
	public static void main(String ...args) {
		V7Driver driver = new V7Driver();
		System.out.println(driver.getAllFiles());
		
		String info = driver.getInodeInfo("/test/result2");
		System.out.println(info);		
		byte[] data = driver.getData("/test/result");
		System.out.println(new String(data));
		
		System.out.println(driver.getSize("/test/result"));
	}
	
	public String getInodeInfo(String path) {
		return extractor.getInodeInfo(path);
	}
	
	public byte[] getData(String path) {
		return extractor.extract(path, false);
	}
	
	public int getSize(String path) {
		return extractor.getSize(path);
	}
	
	public String getAllFiles() {
		return extractor.getValidFiles();
	}

}
