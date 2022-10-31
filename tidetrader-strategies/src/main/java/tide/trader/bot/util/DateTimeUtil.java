/**
 * Copyright &copy; 2012-2014 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package tide.trader.bot.util;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 日期工具类, 继承org.apache.commons.lang.time.DateUtils类
 */
public class DateTimeUtil extends org.apache.commons.lang3.time.DateUtils {
	
	protected static final org.slf4j.Logger log = LoggerFactory.getLogger(DateTimeUtil.class);

	public static final String YYYY_MM_DD = "yyyy-MM-dd";
	public static final String YYMMDD = "yyMMdd";
	public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	public static final String YYYY$MM$DD_HH_MM_SS = "yyyy/MM/dd HH:mm:ss";
	public static final String YYYY_MM_DD_HH_MM_SS_S = "yyyy-MM-dd HH:mm:ss.S";
	public static final String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss:SSS";
	public static final String YYMMDDHHMMSS = "yyMMddHHmmss";
	public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	public static final String YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";
	public static final String cn_YYYY_MM_DD = "yyyy年MM月dd日";
	public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
	public static final String HH_MM_SS = "HH:mm:ss";
	public static final String[] MONTHS = {"一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"};
	public static final String YYYYMM = "yyyyMM";
	public static final String YYYYMMDD = "yyyyMMdd";

	public static final String YYYY_MM_DD_START = "yyyy-MM-dd 00:00:00";
	public static final String YYYY_MM_DD_END = "yyyy-MM-dd 23:59:59";

	private static String[] parsePatterns = {
			"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
			"yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
			"yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

	/**
	 * 得到当前日期字符串 格式（yyyy-MM-dd）
	 */
	public static String getDate() {
		return getDate("yyyy-MM-dd");
	}

	/**
	 * 得到当前日期字符串 格式（yyyy-MM-dd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"
	 */
	public static String getDate(String pattern) {
		return DateFormatUtils.format(new Date(), pattern);
	}

	/**
	 * 将时间戳转为日期并格式化
	 * by guhai
	 * @param ms      时间戳毫秒
	 * @param pattern 日期格式
	 * @return
	 */
	public static String getDate(Long ms, String pattern) {
		if (ms == null || ms.longValue() == 0) {
			return "";
		}
		return DateFormatUtils.format(new Date(ms), pattern);
	}
	/**
	 * 得到日期字符串 默认格式（yyyy-MM-dd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"
	 */
	public static String formatDate(Date date, Object... pattern) {
		String formatDate = null;
		if (pattern != null && pattern.length > 0) {
			formatDate = DateFormatUtils.format(date, pattern[0].toString());
		} else {
			formatDate = DateFormatUtils.format(date, "yyyy-MM-dd");
		}
		return formatDate;
	}

	/**
	 * 得到日期时间字符串，转换格式（yyyy-MM-dd HH:mm:ss）
	 */
	public static String formatDateTime(Date date) {
		return formatDate(date, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 得到当前时间字符串 格式（HH:mm:ss）
	 */
	public static String getTime() {
		return formatDate(new Date(), "HH:mm:ss");
	}

	/**
	 * 得到当前日期和时间字符串 格式（yyyy-MM-dd HH:mm:ss）
	 */
	public static String getDateTime() {
		return formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 得到当前年份字符串 格式（yyyy）
	 */
	public static String getYear() {
		return formatDate(new Date(), "yyyy");
	}

	/**
	 * 得到当前月份字符串 格式（MM）
	 */
	public static String getMonth() {
		return formatDate(new Date(), "MM");
	}

	/**
	 * 得到当天字符串 格式（dd）
	 */
	public static String getDay() {
		return formatDate(new Date(), "dd");
	}

	/**
	 * 得到当天目前时间点小时对应的字符串 格式（HH）
	 */
	public static String getHour(){
		return formatDate(new Date(), "HH");
	}

	/**
	 * 得到当前星期字符串 格式（E）星期几
	 */
	public static String getWeek() {
		return formatDate(new Date(), "E");
	}

	/**
	 * 日期型字符串转化为日期 格式
	 * { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm",
	 *   "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm",
	 *   "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm" }
	 */
	public static Date parseDate(Object str) {
		if (str == null || StringUtils.isBlank(str.toString())){
			return null;
		}
		try {
			return parseDate(str.toString(), parsePatterns);
		} catch (ParseException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 格式化
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String format(Date date, String pattern) {
		if(date==null){
			return "";
		}
		DateFormat format = new SimpleDateFormat(pattern);
		return format.format(date);
	}

	/**
	 * 格式化
	 * @param date
	 * @param parsePattern
	 * @param returnPattern
	 * @return
	 */
	public static String format(String date, String parsePattern, String returnPattern) {
		return format(parse(date, parsePattern), returnPattern);
	}

	/**
	 * 解析
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static Date parse(String date, String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		try {
			return format.parse(date);
		} catch (ParseException e) {
			log.error("ToolDateTime.parse异常：date值" + date + "，pattern值" + pattern, e);
			return null;
		}
	}

	/**
	 * 获取日期中的年月日
	 * @param date 格式：yyyy-MM-dd HH:mm:ss
	 * @return 格式yyyy-MM-dd
	 */
	public static Date parse(Date date) {
		String dateStr = format(date, DateTimeUtil.YYYY_MM_DD_HH_MM_SS);
		SimpleDateFormat format = new SimpleDateFormat(DateTimeUtil.YYYY_MM_DD);
		try {
			return format.parse(dateStr.split(" ")[0]);
		} catch (ParseException e) {
			log.error("ToolDateTime.parse异常：date值" + dateStr.split(" ")[0] + "，pattern值" + DateTimeUtil.YYYY_MM_DD, e);
			return null;
		}
	}

	/**
	 * 获取过去的天数
	 * @param date
	 * @return
	 */
	public static long pastDays(Date date) {
		long t = new Date().getTime()-date.getTime();
		return t/(24*60*60*1000);
	}

	/**
	 * 获取过去的小时
	 * @param date
	 * @return
	 */
	public static long pastHour(Date date) {
		long t = new Date().getTime()-date.getTime();
		return t/(60*60*1000);
	}

	/**
	 * 获取过去的分钟
	 * @param date
	 * @return
	 */
	public static long pastMinutes(Date date) {
		long t = new Date().getTime()-date.getTime();
		return t/(60*1000);
	}

	/**
	 * 获取过去的毫秒数
	 * @param date
	 * @return
	 */
	public static long pastMilliseconds(Date date) {
		long t = new Date().getTime()-date.getTime();
		return t;
	}

	/**
	 * 转换为时间（天,时:分:秒.毫秒）
	 * @param timeMillis
	 * @return
	 */
	public static String formatDateTime(long timeMillis){
		if(timeMillis == 0){
			return "";
		}
		long day = timeMillis/(24*60*60*1000);
		long hour = (timeMillis/(60*60*1000)-day*24);
		long min = ((timeMillis/(60*1000))-day*24*60-hour*60);
		long s = (timeMillis/1000-day*24*60*60-hour*60*60-min*60);
		long sss = (timeMillis-day*24*60*60*1000-hour*60*60*1000-min*60*1000-s*1000);
		return (day>0?day+",":"")+hour+":"+min+":"+s+"."+sss;
	}

	/**
	 * bigint类型进来之前先除以1000
	 * 转换为时间（天,时:分:秒.毫秒）
	 * @param timeMillis
	 * @return
	 */
	public static String formatDateTimeByThousand(long timeMillis){
		if(timeMillis == 0){
			return "";
		}
		timeMillis = timeMillis/1000;
		return formatDateTime(timeMillis);
	}
	/**
	 * 转换为时间（时:分）
	 * @param timeMillis
	 * @return
	 */
	public static String formatDateTimeRvsss(long timeMillis){
//    	long day = timeMillis/(24*60*60*1000);
		long hour = (timeMillis/(60*60*1000));
		double min = ((timeMillis/(60.0*1000))-hour*60.0);
//    	long s = (timeMillis/1000-hour*60*60-min*60);
//    	long sss = (timeMillis-day*24*60*60*1000-hour*60*60*1000-min*60*1000-s*1000);
		return ( hour>0?hour+"小时":"") +( min>0?new BigDecimal(min).setScale(2, RoundingMode.HALF_UP).doubleValue()+"分钟":"");
	}

	/**
	 * 获取两个日期之间的天数
	 *
	 * @param before
	 * @param after
	 * @return
	 */
	public static double getDistanceOfTwoDate(Date before, Date after) {
		if(before == null || after == null){
			return 0;
		}

		long beforeTime = before.getTime();
		long afterTime = after.getTime();
		return (afterTime - beforeTime) / (1000 * 60 * 60 * 24);
	}

	/**
	 * @Description: 比较两个日期大小
	 * @param before
	 * @param after
	 * @return -1前者小于后者 0前者等于后者 1前者大于后者
	 */
	public static int compareTowDays(Date before, Date after) {
		try {
			if(before == null || after == null){
				return 0;
			}

			if (before.getTime() > after.getTime()) {
				return 1;
			} else if (before.getTime() < after.getTime()) {
				return -1;
			} else {
				return 0;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return 0;
		}
	}

	/**
	 * 获取两个日期之间的分钟
	 * @param before
	 * @param after
	 * @return
	 */
	public static double getDistanceMinuteOfTwoDate(Date before, Date after) {

		if(before == null || after == null){
			return 0;
		}

		long beforeTime = before.getTime();
		long afterTime = after.getTime();

		double tmpMt = (afterTime - beforeTime) / (1000 * 60.0);

		return new BigDecimal(tmpMt).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	/**
	 * 获取两个日期之间的小时
	 * @param before
	 * @param after
	 * @return
	 */
	public static double getDistanceHourOfTwoDate(Date before, Date after) {
		if(before == null || before == null){
			return 0;
		}

		long beforeTime = before.getTime();
		long afterTime = after.getTime();
		return (afterTime - beforeTime) / (1000 * 60 * 60);
	}

	/**
	 * 获取两个日期之间的小时,保留2位小数，四舍五入
	 * @param before
	 * @param after
	 * @return
	 */
	public static double getHoursOfTwoDate(Date before, Date after) {
		if(before == null || before == null){
			return 0;
		}
		long beforeTime = before.getTime();
		long afterTime = after.getTime();

		return (new BigDecimal(afterTime).subtract(new BigDecimal(beforeTime))).
				divide((new BigDecimal(1000).multiply(new BigDecimal(60).multiply(new BigDecimal(60)))), 2, RoundingMode.HALF_UP).doubleValue();
	}

	/**
	 * 获取两个日期之间的秒
	 * @param before
	 * @param after
	 * @return
	 */
	public static double getDistanceSecondsOfTwoDate(Date before, Date after) {
		if(before == null || before == null){
			return 0;
		}

		long beforeTime = before.getTime();
		long afterTime = after.getTime();
		return (afterTime - beforeTime) / 1000;
	}

	/**
	 * @Description: 时间戳格式化为日期字符串
	 * @param timestamp
	 * @return
	 */
	public static String formatDateFromUnix(long timestamp, String pattern){
		if(timestamp > 0){
			if(StringUtils.isBlank(pattern)){
				pattern = YYYY_MM_DD_HH_MM_SS;
			}
			return format(new Date(timestamp), pattern);
		}
		return null;
	}

	/**
	 * 将YYYY_MM_DD_HH_MM_SS格式的字符串转化为毫秒数
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static long getseconds(String date,String pattern){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		long time = 0;
		try {
			time = simpleDateFormat.parse(date).getTime();
		} catch (ParseException e) {
			log.error(e.getMessage(), e);
			// delete e.print
		}
		return time;

	}


	/**
	 * 将指定毫秒数转化为YYYY_MM_DD_HH_MM_SS格式的字符串
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String getDateBySeconds(long date,String pattern){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String time = null;
		try {
			time = simpleDateFormat.format(date);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			// delete e.print
		}
		return time;

	}

	/**
	 * 获取两个日期之间的月份
	 * @param before
	 * @param after
	 * @return
	 */
	public static int getMonthOfTwoDate(Date before,Date after){
		Calendar bef = Calendar.getInstance();
		Calendar aft = Calendar.getInstance();
		bef.setTime(before);
		aft.setTime(after);
		int result = aft.get(Calendar.MONTH) - bef.get(Calendar.MONTH);
		int month = (aft.get(Calendar.YEAR) - bef.get(Calendar.YEAR)) * 12;
		return Math.abs(month + result);
	}

	public static Date parseDateFormUnix(long timeStamp, String pattern){
		if(timeStamp == 0l){
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		Long time = Long.valueOf(timeStamp);
		String d = format.format(time);
		Date date;
		try {
			date = format.parse(d);
			return date;
		} catch (ParseException e) {
			log.error(e.getMessage(), e);
			// delete e.print
		}
		return null;
	}

	/**
	 * 转 YYYY_MM_DD_HH_MM_SS
	 * @param timeStamp
	 * @return
	 */
	public static Date parseDateFormUnix(long timeStamp){
		return parseDateFormUnix(timeStamp, YYYY_MM_DD_HH_MM_SS);
	}

	/**
	 * 转 YYYY_MM_DD_HH_MM_SS
	 * @param timeStamp
	 * @return
	 */
	public static Date parseDateFormUnix(Long timeStamp){
		if (timeStamp == null) {
			return null;
		}
		return parseDateFormUnix(timeStamp, YYYY_MM_DD_HH_MM_SS);
	}

	/**
	 * 获取两个时间之间日期集合
	 * @param before
	 * @param after
	 * @return
	 */
	public static List<String> getMonthListOfTwoDate(Date before, Date after){
		int months = getMonthOfTwoDate(before, after);

		if(months <= 0){
			return new ArrayList<String>();
		}

		Calendar afterCal = Calendar.getInstance();
		afterCal.setTime(after);

		List<String> monthList = Lists.newArrayList();
		for(int i=0; i< months; i++){
			afterCal.set(Calendar.MONTH, afterCal.get(Calendar.MONTH) - 1);
			monthList.add(DateTimeUtil.format(afterCal.getTime(), "yyyyMM"));
		}

		return monthList;
	}

	/**
	 * 获得两个时间之间的日期集合，包含最小月份,不包含最大月份
	 * @param before
	 * @param after
	 * @return
	 */
	public static List<String> getMonthListOfTwoDates(Date before, Date after){
		int months = getMonthOfTwoDate(before, after);

		if(months < 0){
			return new ArrayList<String>();
		}
		List<String> monthList = Lists.newArrayList();
		for (int i = 0; i < months; i++) {
			after = addMonths (after,-1);
			monthList.add(DateTimeUtil.format(after, "yyyyMM"));
		}
//		Calendar afterCal = Calendar.getInstance();
//		afterCal.setTime(after);
//
//		List<String> monthList = Lists.newArrayList();
//		for(int i=0; i< months; i++){
//			monthList.add(DateUtils.format(afterCal.getTime(), "yyyyMM"));
//			afterCal.set(Calendar.MONTH, afterCal.get(Calendar.MONTH) - 1);
//		}

		return monthList;
	}


	/*
		获取当前日期月份的开始时间
	 */
	public static Date getMonthStartDate(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}

	/*
    获取当前日期月份的截至时间
 */
	public static Date getMonthEndDate(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(DateTimeUtil.getMonthStartDate(date));
		//将当前月加1
		calendar.add(Calendar.MONTH, 1);
		//在当前月的下一月基础上减去1秒
		calendar.add(Calendar.SECOND, -1);
		return calendar.getTime();
	}



	/**
	 * 获取当前日期上一个的月份   格式yyyyMM
	 * @return
	 */
	public static String getPreviousMonth(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYYMM);
		String date = LocalDate.now().minusMonths(1).format(formatter);
		return date;
	}

}
