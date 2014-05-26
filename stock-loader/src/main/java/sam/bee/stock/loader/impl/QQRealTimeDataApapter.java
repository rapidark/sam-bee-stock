package sam.bee.stock.loader.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class QQRealTimeDataApapter implements IDataAdapter{

//	private static final String[] FIELDS = {
//		"未知",	//		0: 未知
//		"名字",	//		1: 名字
//		"代码",	//		2: 代码 
//		"当前价格",//		3: 当前价格
//		"涨跌",//		4: 涨跌
//		"涨跌%",//		5: 涨跌% 
//		"成交量（手）",//		6: 成交量（手） 
//		"成交额（万）",//		7: 成交额（万）
//		 "",		  //		8: 
//		"总市值"     //9: 总市值
//	};
	
	private static final String[] FIELDS = {
	"未知", 		//0: 未知
	"STOCK_NAME",			//	 1: 名字
	"STOCK_CODE",			//	 2: 代码
	"STOCK_PRICE",		//	 3: 当前价格
	"PREVIOUS_CLOSE",			//	 4: 昨收
	"OPEN",			//	 5: 今开
	"VOLUMN",	//	 6: 成交量（手）
	"外盘",			//	 7: 外盘
	"内盘",			//	 8: 内盘
	"BUY1_PRICE",			//	 9: 买一
	"BUY1_VOLUMN",	//	10: 买一量（手）
	"BUY2_PRICE",			//	11
	"BUY2_VOLUMN",	//	12
	"BUY3_PRICE",			//	13
	"BUY3_VOLUMN",		//	14
	"BUY4_PRICE",			//	15
	"BUY4_VOLUMN",	//	16
	"BUY5_PRICE",			//	17
	"BUY5_VOLUMN", //18: 买五
	"SELL1_PRICE",	//	19: 卖一
	"SELL1_VOLUMN",	//	20: 卖一量
	"SELL2_PRICE",	//	21: 卖二 卖五
	"SELL2_VOLUMN",	//	22: 卖二 卖五
	"SELL3_PRICE",	//	23: 卖二 卖五
	"SELL3_VOLUMN",	//	24: 卖二 卖五
	"SELL4_PRICE",	//	25: 卖二 卖五
	"SELL4_VOLUMN",	//	26: 卖二 卖五
	"SELL5_PRICE",	//	27: 卖二 卖五
	"SELL5_VOLUMN",	//	28: 卖二 卖五
	"最近逐笔成交",	//	29: 最近逐笔成交
	"CREATED_TIME",	//	30: 时间
	"CHANGE",	//	31: 涨跌
	"CHANGE_PERCENT",	//	32: 涨跌%
	"最高",	//	33: 最高
	"最低",	//	34: 最低
	"成交量",	//	35: 价格/成交量（手）/成交额
	"成交量",	//	36: 成交量（手）
	"成交额",	//	37: 成交额（万）
	"换手率",	//	38: 换手率
	"市盈率",	//	39: 市盈率
	"最高",	//	40: 
	"HIGH",	//	41: 最高
	"LOW",	//	42: 最低
	"STOCK_AMPLITUDE",	//	43: 振幅
	"流通市值",	//	44: 流通市值
	"总市值",//	45: 总市值
	"市净率",//	46: 市净率
	"涨停价",//	47: 涨停价
	"跌停价"//	48: 跌停价
	};
	
	@Override
	public List<Map<String, Object>> parse(List<?> list) {
		
		List<Map<String, Object>> ret = new Vector<Map<String,Object>>();
		Map<String, Object> map ;
		for(Object obj : list){
			String tmp = (String)obj;
			
			//System.out.println(tmp);
			
			String[] values = tmp.replaceAll("\"", "").replaceAll(";", "").split("=");
			String[] fields = values[1].split("~");
			map = new HashMap<String, Object>(fields.length);
			for(int i=0;i<fields.length;i++){	
				if(fields[i]!=null && fields[i].length()>0){
					
					//System.out.println(String.format("%d%s%s", i, FIELDS[i], fields[i]));
					map.put(FIELDS[i], fields[i]);
				}
				
			}
			
			ret.add(map);
		}
	
		return ret;
	}
			
}