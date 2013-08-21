package cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

public class ClearCache {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path= new File(".").getAbsolutePath();
		CacheManager manager = null;
		try{
			InputStream fis = new FileInputStream(new File(path+"/ehcache.xml").getAbsolutePath());//We obtain the configuration file of the cache manager.
			manager = CacheManager.create(fis);//Singleton mode, avoid multiple manager with the same configuration.
			fis.close();
			Cache cache = manager.getCache("safetycodecache1"); //Obtain the instance of the configured cache.
			if(cache==null){
				System.out.println("The cache is null");
			}else{
				cache.removeAll();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		if(manager!=null){
			manager.shutdown();
		}
	}

}
