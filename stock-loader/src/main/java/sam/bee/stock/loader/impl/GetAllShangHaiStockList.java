package sam.bee.stock.loader.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class GetAllShangHaiStockList {

	public List<Map<String,String>> list() throws IOException{
		
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		Document doc = Jsoup.connect("http://bbs.10jqka.com.cn/codelist.html").get();
//      Document doc = Jsoup.parse(input, "UTF-8", "");	
		Elements  els = doc.select("div.bbsilst_wei3");
		
		assert(els.size()==3);
		Elements shuanghai = els.get(0).select("a");
		Map<String,String> map;
		
		for(int i=0; i<shuanghai.size(); i++){
			String line = shuanghai.get(i).text();
			if(line!=null && line.length()>0){
				String[] str =line.split(" ");
				map = new HashMap();
				map.put("STOCK_NAME", str[0].trim());
				map.put("STOCK_CODE", str[1].trim());
				list.add(map);
			}
		}
		
		return list;
	}
}