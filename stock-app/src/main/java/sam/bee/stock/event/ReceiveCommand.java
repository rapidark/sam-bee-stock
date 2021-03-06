package sam.bee.stock.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sam.bee.stock.gui.*;
import sam.bee.stock.service.vo.MinReq;
import sam.bee.stock.service.vo.ProductInfoListVO;
import sam.bee.stock.trade.Market;
import sam.bee.stock.vo.MinDataVO;
import sam.bee.stock.vo.ProductDataVO;
import sam.bee.stock.vo.ProductInfoVO;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static sam.bee.stock.Const.*;
import static sam.bee.stock.gui.Application.bDebug;

public class ReceiveCommand extends Thread{
	protected static final Logger logger = LoggerFactory.getLogger(ReceiveCommand.class);
	Application m_application = null;
	SendCommand m_borker;
	

	
	public ReceiveCommand(SendCommand borker) {
		m_application= borker.m_AppMain;
		m_borker = borker;
		receiveCodeTable();
		receiveStockQuote();
	}
	
	@Override
	public void run() {
		while(true){
			getCodeList();
			/** 报价排名*/ receiveClassSort();
			receiveCodeTable();
			//receiveStockQuote();
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 报价排名
	 */
	private void receiveClassSort() {

		logger.info("=== 报价排名 ===");
		
		try {
			ProductDataVO[] vo = getCMDSortVOs();
			byte sortBy = 0;
			byte isDescend = 0;
			int totalCount = vo.length;
			int start =1;   

				if (m_application.iCurrentPage != 0)
					return;
				Page_MultiQuote page = (Page_MultiQuote) m_application.mainGraph;
				if (page.sortBy != sortBy
						|| page.isDescend != isDescend
						|| page.iStart != start){
					return;
				}
					
				page.packetInfo = new Packet_MultiQuote();
				page.packetInfo.currentStockType = 1;
				page.packetInfo.sortBy = sortBy;
				page.packetInfo.isDescend = isDescend;
				page.packetInfo.iStart = start;
				page.packetInfo.iEnd = start + vo.length;
				page.packetInfo.iCount = totalCount;

				if (bDebug)
					System.out.println("totalCount = " + totalCount);
				page.quoteData = vo;
				m_application.repaint();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
      
	}
	

	/**
	 * 
	 */
	private void receiveCodeTable(){

		logger.info("=== 获取代码表 ===");
		ProductInfoListVO productInfoList;
		try {
			productInfoList = getProductInfoListVOs();
			
			for (int i = 0; i < productInfoList.productInfos.length; i++) {
				
				
				boolean bFound = false;
				
				for (int j = 0; j < m_application.m_codeList.size(); j++) {
					if (!productInfoList.productInfos[i].code
							.equalsIgnoreCase((String) m_application.m_codeList
									.elementAt(j)))
						continue;
					bFound = true;
					break;
				}

				if (!bFound){
					m_application.m_codeList.addElement(productInfoList.productInfos[i].code);
				}
				CodeTable codeTable = new CodeTable();
				codeTable.sName = productInfoList.productInfos[i].name;
				codeTable.sPinyin = productInfoList.productInfos[i].pinyin;
				codeTable.status = productInfoList.productInfos[i].status;
				codeTable.fUnit = productInfoList.productInfos[i].fUnit;
				codeTable.tradeSecNo = productInfoList.productInfos[i].tradeSecNo;
				
				m_application.m_ProductByHttp.put(productInfoList.productInfos[i].code, codeTable);
				m_application.m_iCodeDate = productInfoList.date;
				m_application.m_iCodeTime = productInfoList.time;
			}		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	 
	}
	
	/**
	 *  个股行情
	 */
	@SuppressWarnings("unchecked")
	private void receiveStockQuote()  {
		
		
		try {

		logger.info("=== 接收个股行情 ===");
		ProductDataVO[] vo = getProductDataVOs();
	
		String sCode = "";
		for (int i = 0; i < vo.length; i++) {
			sCode = vo[i].code;
			ProductData stock = m_application.getProductData(sCode);
			if (stock == null) {
				if (m_application.vProductData.size() > 50)
					m_application.vProductData.removeElementAt(50);
				stock = new ProductData();
				stock.sCode = sCode;
				stock.realData = vo[i];
				m_application.vProductData.insertElementAt(stock, 0);
			} else {
				stock.realData = vo[i];
			}
		}

		if (vo.length > 0
				&& (2 == m_application.iCurrentPage || 1 == m_application.iCurrentPage)
				&& m_application.strCurrentCode.equals(sCode)){
			m_application.repaint();
		}
		if (vo.length > 0 && m_application.m_bShowIndexAtBottom == 1
				&& m_application.indexMainCode.length() > 0
				&& vo[0].code.equalsIgnoreCase(m_application.indexMainCode)){
			m_application.repaintBottom();
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	   @SuppressWarnings({ "unchecked"})
		private void getCodeList() {
	        for(boolean bSucceed = false; m_application != null && m_application.bRunning && !bSucceed;) {
	            try {
	                ProductInfoListVO list =  getProductInfoListVOs();
	             
	                if(bDebug){
						logger.info("=== 获取股票代码信息 ==="  + list.date + " " + list.time);
	                }
	                m_application.m_iCodeDate = list.date;
	                m_application.m_iCodeTime = list.time;
	                ProductInfoVO products[] = list.productInfos;
	                m_application.m_codeList.removeAllElements();
	                m_application.m_ProductByHttp.clear();
	                for(int i = 0; i < products.length; i++) {
	                    m_application.m_codeList.addElement(products[i].code);
	                    CodeTable data = new CodeTable();
	                    data.sName = products[i].name;
	                    data.sPinyin = products[i].pinyin;
	                    data.status = products[i].status;
	                    data.tradeSecNo = products[i].tradeSecNo;
	                    data.fUnit = products[i].fUnit;
	                    m_application.m_ProductByHttp.put(products[i].code, data);
	                    if(data.status == 3 && m_application.indexMainCode.length() == 0){
	                        m_application.indexMainCode = products[i].code;
	                    }
	                }

	                bSucceed = true;
	                m_application.repaint();
	            }
	            catch(MalformedURLException malformedurlexception) { }
	            catch(IOException ex) {                
	                if(bDebug){
	                    ex.printStackTrace();
	                }
	            }
	            catch(Exception ex) {
	                if(bDebug){
	                    ex.printStackTrace();
	                }
	            }
	            if(!bSucceed){
	                try {Thread.sleep(1000L);}catch(InterruptedException interruptedexception) { }
	            }
	        }

	    }


		final static String STOCK_DATA_TABLE= "STOCK_DATA";

		
	   public ProductInfoListVO getProductInfoListVOs() throws Exception {

			ProductInfoListVO productInfoList = new ProductInfoListVO();
			try {
				if(bDebug){
					logger.info("=== getProductInfoListVOs ===" );
				}
				List<Map<String,String>> data  = Application.getInstance().market.getStockInfos();
			       productInfoList.date = 2016;
			        productInfoList.time = 1200;

			        ProductInfoVO productInfos[] = new ProductInfoVO[data.size()];
			        for(int i = 0; i < productInfos.length; i++) {
			        	  productInfos[i] = new ProductInfoVO();
			        	  Map<String,String> m = data.get(i);
			              productInfos[i].code =m.get(STOCK_CODE);
			              productInfos[i].status = 1;
			              productInfos[i].fUnit = 2;
			              productInfos[i].name = m.get(STOCK_NAME);
			              productInfos[i].pinyin = new String[4];
			              
			              for(int j = 0; j < productInfos[i].pinyin.length; j++){
			            	  productInfos[i].pinyin[j] =  productInfos[i].code ;
			              }
			              
			              productInfos[i].tradeSecNo = new int[4];
			              for(int j = 0; j < productInfos[i].tradeSecNo.length; j++)
			                  productInfos[i].tradeSecNo[j] =  i;

			              if(productInfos[i].fUnit <= 0.0F)
			                  productInfos[i].fUnit = 1.0F;
			        }
			        
			        productInfoList.productInfos = productInfos;
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			return productInfoList;
	    }

		/**
		 * 获取实时股价信息
		 * @return
		 * @throws Exception
		 */
	   public static ProductDataVO[] getProductDataVOs() throws Exception {
		   Market market = Application.getInstance().market;
		   if(bDebug){
			   logger.info("=== 获取实时股价数据 ===" + market.getCurrentDate());
		   }
	    	List<Map<String, String>> data = market.getCurrentData();


		   //这里实现实时的数据加载，可以使用不同的数据提供给界面显示数据。
	    	ProductDataVO vo[] = new ProductDataVO[data.size()];
	        for(int i = 0; i < vo.length; i++) {
	        	Map<String,String> m = data.get(i);
	        	vo[i] = new ProductDataVO();
	        	vo[i].code =m.get(STOCK_CODE);
				vo[i].name =m.get(STOCK_NAME);
	        	vo[i].time = new Date();
	        	vo[i].closePrice =  Float.valueOf(m.get(CLOSE));
	        	vo[i].openPrice = Float.valueOf(m.get(OPEN));
	        	vo[i].highPrice = Float.valueOf(m.get(HIGH));
	        	vo[i].lowPrice =  Float.valueOf(m.get(LOW));
	        	vo[i].curPrice =  5.0F;
	        	vo[i].totalAmount = 6L;
	        	vo[i].totalMoney =  7.0D;
	        	vo[i].curAmount = 8;
	        	vo[i].amountRate =  9.0F;
	        	vo[i].balancePrice =  10.0F;
	        	vo[i].reserveCount = 11;
	        	vo[i].yesterBalancePrice =  12.0F;
	        	vo[i].reserveChange = 13;
	            if(true) {
	            	vo[i].inAmount = 0;
	                vo[i].outAmount = 0;
	                vo[i].buyAmount = new int[1];
	                for(int j = 0; j < vo[i].buyAmount.length; j++)
	                	vo[i].buyAmount[j] = 0;

	                vo[i].sellAmount = new int[1];
	                for(int j = 0; j < vo[i].sellAmount.length; j++)
	                	vo[i].sellAmount[j] = 0;

	                vo[i].buyPrice = new float[1];
	                for(int j = 0; j < vo[i].buyPrice.length; j++)
	                	vo[i].buyPrice[j] = 0;

	                vo[i].sellPrice = new float[1];
	                for(int j = 0; j < vo[i].sellPrice.length; j++)
	                	vo[i].sellPrice[j] = 0;

	            }
	        }
	        return vo;
	    }
	   
	   public static ProductDataVO[] getCMDSortVOs() throws Exception {
		    logger.info("=== 获取排序的实时股票信息 ===");
	    	List<Map<String, String>> data;

	    	data = Application.getInstance().market.getCurrentData();
			  ProductDataVO[] vo = new ProductDataVO[data.size()];
		        for(int i = 0; i < vo.length; i++) {
		        	vo[i] = new ProductDataVO();
		            Map<String,String> m = data.get(i);
		            vo[i].code = m.get(STOCK_CODE);
		            vo[i].name = m.get(STOCK_NAME);
		            vo[i].yesterBalancePrice = 1F;
					vo[i].closePrice =  Float.valueOf(m.get(CLOSE));
					vo[i].openPrice = Float.valueOf(m.get(OPEN));
					vo[i].highPrice = Float.valueOf(m.get(HIGH));
					vo[i].lowPrice =  Float.valueOf(m.get(LOW));
					//最新
		            vo[i].curPrice = Float.valueOf(m.get(CLOSE)).floatValue();
		            vo[i].totalAmount = Long.valueOf(m.get(VOLUME));
		            vo[i].totalMoney = 7D;
		            vo[i].curAmount = 8;
		            vo[i].amountRate = 9F;
		            vo[i].balancePrice = 10F;
		            vo[i].reserveCount =11;
		            vo[i].buyAmount = new int[1];
		            vo[i].buyAmount[0] = 1;
		            vo[i].sellAmount = new int[1];
		            vo[i].sellAmount[0] = 1;
		            vo[i].buyPrice = new float[1];
		            vo[i].buyPrice[0] = 1F;
		            vo[i].sellPrice = new float[1];
		            vo[i].sellPrice[0] = 1F;
		        }
		        return vo;
	    }
	   
	   
   /**
    * 分时数据
    * @param reader
    * @throws IOException
    */
	private void receiveMinLineData(DataInputStream reader) throws IOException {
		logger.info("=== 获取分钟数据 ===");
		String code = reader.readUTF();
		byte type = reader.readByte();
		int time = reader.readInt();
		MinDataVO values[] = MinReq.getObj(reader);
		ProductData stock = m_application.getProductData(code);
		if (stock == null) {
			if (m_application.vProductData.size() > 50)
				m_application.vProductData.removeElementAt(50);
			stock = new ProductData();
			stock.sCode = code;
			m_application.vProductData.insertElementAt(stock, 0);
		}
		stock.vMinLine = new Vector();
		int jMin = 0;
		for (int i = 0; i < values.length; i++) {
			int iIndex = Common.GetMinLineIndexFromTime(values[i].time,
					m_application.m_timeRange, m_application.m_iMinLineInterval);
			for (int j = jMin; j < iIndex; j++) {
				MinDataVO min = new MinDataVO();
				if (j > 0) {
					min.curPrice = ((MinDataVO) stock.vMinLine.elementAt(j - 1)).curPrice;
					min.totalAmount = ((MinDataVO) stock.vMinLine
							.elementAt(j - 1)).totalAmount;
					min.totalMoney = ((MinDataVO) stock.vMinLine
							.elementAt(j - 1)).totalMoney;
					min.averPrice = ((MinDataVO) stock.vMinLine
							.elementAt(j - 1)).averPrice;
					min.reserveCount = ((MinDataVO) stock.vMinLine
							.elementAt(j - 1)).reserveCount;
				} else if (stock.realData != null) {
					min.curPrice = stock.realData.yesterBalancePrice;
					min.averPrice = stock.realData.yesterBalancePrice;
				}
				stock.vMinLine.addElement(min);
			}

			if (iIndex >= stock.vMinLine.size() - 1) {
				MinDataVO min = null;
				if (iIndex == stock.vMinLine.size() - 1) {
					min = (MinDataVO) stock.vMinLine.lastElement();
				} else {
					min = new MinDataVO();
					stock.vMinLine.addElement(min);
				}
				min.curPrice = values[i].curPrice;
				min.totalAmount = values[i].totalAmount;
				min.reserveCount = values[i].reserveCount;
				min.averPrice = values[i].averPrice;
				jMin = iIndex + 1;
			}
		}

		if ((2 == m_application.iCurrentPage || 1 == m_application.iCurrentPage)
				&& m_application.strCurrentCode.equals(stock.sCode))
			m_application.repaint();
	}
	
   /**
    * Get 5 min data.
    * @return
    * @throws Exception
    */
   MinDataVO[] getMinDataVO() throws Exception {

	   logger.info("=== 获取分钟数据 ===");
     MinDataVO mins[] = new MinDataVO[10];
     for(int i = 0; i < mins.length; i++) {
         mins[i] = new MinDataVO();
         mins[i].time = 1200;
         mins[i].curPrice = i;
         mins[i].totalAmount = i;
         mins[i].averPrice = i;
         mins[i].reserveCount = i;
     }

     return mins;
 }
}
