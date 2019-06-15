import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
	
	private static String fileEncode = "utf8";
	
    private static String songFilePath = "../songs";

    private static String totalRecordFilePath = "../每日汇总.txt";

    private static String songsSortedByTimeFilePath = "../单曲统计-时间排序.txt";
	
	private static String songsSortedByTimesFilePath = "../单曲统计-次数排序.txt";

    private static ArrayList<File> fileList = new ArrayList<>();

    private static ArrayList<Record> totalRecord = new ArrayList<>();

    private static HashMap<String,Song> songs = new HashMap<>();

    private static ArrayList<Map.Entry<String,Song>> sortedSongs;

    public static void main(String[] args) {
        scanSongSheet();
        readSongSheet();
        sortTotalRecord();
        calcCountsOfSong();
        calcLastTimeOfSong();
		writeTotalRecordToFile();
		
        sortSongsByTime();
		writeSortedSongsToFile(songsSortedByTimeFilePath);
		
		sortSongsByTimes();
        writeSortedSongsToFile(songsSortedByTimesFilePath);
		
        System.out.println("输出结果:");
		System.out.println(new File(totalRecordFilePath).getAbsolutePath());
		System.out.println(new File(songsSortedByTimeFilePath).getAbsolutePath());
		System.out.println(new File(songsSortedByTimesFilePath).getAbsolutePath());
        System.out.println("------完成-------");
    }

    public static void scanSongSheet(){
        for (File file : new File(songFilePath).listFiles()) {
            if(file.isDirectory()){
                for(File file1:file.listFiles()){
                    if(file1.isDirectory()){
						for(File file2:file1.listFiles()){
							fileList.add(file2);	
						}
					}
                }
            }
        }
    }

    public static void readSongSheet(){
        FileInputStream in = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        for(File file : fileList){
            try {
                isr = new InputStreamReader(new FileInputStream(file), fileEncode);
                br = new BufferedReader(isr);

                String songName = "";
                String dateString = file.getName().substring(0,file.getName().indexOf("."));
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);

                while ((songName = br.readLine()) != null) {
                    if(songName.indexOf(".") < 0){
                        continue;
                    }

                    //获取序号
                    String seq = songName.split("\\.")[0].replace(" ","");
					
					//格式化歌名
                    songName = formatName(songName);
					
					if(songName.equals("")){
						continue;
					}
					
					//特殊歌名处理
                    songName = handleSpecialSong(songName);
					
                    songs.put(songName,new Song(songName));
                    totalRecord.add(new Record(Integer.valueOf(seq),songName,date));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void sortTotalRecord(){
        Collections.sort(totalRecord, (Record o1, Record o2)->{
            if(o1.getTime().getTime() < o2.getTime().getTime()){
                return 1;
            }else if(o1.getTime().getTime() > o2.getTime().getTime()){
                return -1;
            }else{
                if(o1.getSeq() > o2.getSeq()){
                    return 1;
                }else if(o1.getSeq() < o2.getSeq()){
                    return -1;
                }
            }
			return 0;
        });
    }

    public static void calcCountsOfSong() {
        totalRecord.forEach(record -> {
            String name = record.getName();
            Song song = songs.get(name);
            song.setTimes(song.getTimes() + 1);
        });
    }

    public static void calcLastTimeOfSong(){
        songs.forEach((name,song)->{
            song.setLastTime(totalRecord.get(totalRecord.indexOf(new Record(name))).getTime());
        });
    }

    public static void sortSongsByTime(){
        ArrayList<Map.Entry<String,Song>> list = new ArrayList<Map.Entry<String,Song>>(songs.entrySet());
        Collections.sort(list,(o1,o2)->{
            Song v1 = o1.getValue();
            Song v2 = o2.getValue();
            if(v1.getLastTime().getTime() < v2.getLastTime().getTime()){
                return 1;
            }else if(v1.getLastTime().getTime() > v2.getLastTime().getTime()){
                return -1;
            }else{
                if(v1.getTimes() < v2.getTimes()){
                    return 1;
                }else if(v1.getTimes() > v2.getTimes()){
                    return -1;
                }
            }
			return 0;
        });
        sortedSongs = list;
    }
	
	public static void sortSongsByTimes(){
        ArrayList<Map.Entry<String,Song>> list = new ArrayList<Map.Entry<String,Song>>(songs.entrySet());
        Collections.sort(list,(o1,o2)->{
            Song v1 = o1.getValue();
            Song v2 = o2.getValue();
			if(v1.getTimes() < v2.getTimes()){
				return 1;
			}else if(v1.getTimes() > v2.getTimes()){
				return -1;
			}else{
				if(v1.getLastTime().getTime() < v2.getLastTime().getTime()){
					return 1;
				}else if(v1.getLastTime().getTime() > v2.getLastTime().getTime()){
					return -1;
				}
			}
			return 0;
        });
        sortedSongs = list;
    }

    public static void writeTotalRecordToFile(){
        try(OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(totalRecordFilePath), fileEncode)) {
            osw.write("序号 |   时间    |序号| 歌名\r\n");
			for(int i =0,size = totalRecord.size(); i < size;i++){
				Record record = totalRecord.get(i);
				try {
                     osw.append(String.format("%-5s", (i+1)) + "|" + new SimpleDateFormat("yyyy-MM-dd").format(record.getTime()) + " | " + String.format("%-2s", record.getSeq()) + " | " + record.getName() 
                            + "\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
			}
            osw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeSortedSongsToFile(String path){
		
        try(OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(path), fileEncode)) {
            osw.write("序号| 最后一次  |次数 | 歌名 \r\n");
			for(int i =0,size = sortedSongs.size(); i < size;i++){
				Song s = sortedSongs.get(i).getValue();
				try {
                    osw.append(String.format("%-4s", (i+1)) + "|" +new SimpleDateFormat("yyyy-MM-dd").format(s.getLastTime()) + " | " + String.format("%-3s", s.getTimes()) + " | "+  s.getName()  + "\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
			}
            osw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String formatName(String name){
        //去除序号
        name = name.substring(name.indexOf(".") + 1, name.length());

        //去除括号内容
        if(name.indexOf("（") > -1){
            name = name.substring(0,name.indexOf("（"));
        }
        if(name.indexOf("(") > -1){
            name = name.substring(0,name.indexOf("("));
        }

        //去除歌手
        if(name.indexOf("-") > -1){
            name = name.substring(0,name.indexOf("-"));
        }
		
		//去除方括号内容
		if(name.indexOf("【") > -1){
            name = name.substring(0,name.indexOf("【"));
        }
		
        //去除首尾空格，转换成小写
        name = name.trim().toLowerCase();
        
		//特殊符号处理
        name = name.replace(" + ","+")
			.replace(" ＋ ","+")
            .replace("＋","+")
			.replace(" & ","&")
            .replace("dont ","don't ")
			.replace("its ","it's ")
			.replace("don’t ","don't ")
			.replace("it’s ","it's ");
        return name;
    }

    public static String handleSpecialSong(String name){
        if(name.contains("despacito") || name.contains("despasito") || name.equals("慢慢来")){
            name = "despacito中文版-慢慢来";
        }
        if(name.contains("see you again")){
            name = "see you again";
        }
        if(name.contains("i feel close to you")){
            name = "tonight i feel close to you";
        }
        if(name.contains("love you like a song")){
            name = "love you like a love song";
        }
        if(name.contains("let me love you")){
            name = "let me love you & faded";
        }
        if(name.contains("a.i.n.y.") || name.contains("a.i.n.y")){
            name = "a.i.n.y.";
        }
		if(name.contains("bingbian")){
            name = "病变";
        }
		if(name.contains("知马俐")){
            name = "陆垚知马俐";
        }
        if(name.contains("烟熏妆")){
            name = "mascara";
        }
		if(name.contains("cheap thrills")){
            name = "cheap thrills";
        }
		if(name.equals("et") || name.equals("e.t")){
            name = "e.t.";
        }
        if(name.equals("爱你等于爱自己")){
            name = "爱你就等于爱自己";
        }
        if(name.equals("我喜欢上你的内心活动") || name.equals("我喜欢上你是的内心活动")){
            name = "我喜欢上你时的内心活动";
        }
        if(name.equals("明天你好")){
            name = "明天，你好";
        }
        if(name.equals("love！")){
            name = "love!";
        }
        if(name.equals("绿柚子")){
            name = "绿袖子";
        }
        if(name.equals("星语星愿")){
            name = "星语心愿";
        }
        if(name.equals("一人我饮酒醉")){
            name = "一人饮酒醉";
        }
		if(name.equals("dilemma") || name.equals("my dilemma 2.0")){
            name = "my dilemma";
        }
        if(name.equals("柠檬树")){
            name = "lemon tree";
        }
        if(name.equals("想你的三百六十五天")){
            name = "想你的365天";
        }
        if(name.equals("灌篮高手") || name.equals("好想大声说爱你") || name.equals("好想大声说喜欢你")){
            name = "好想大声说爱你";
        }
        if (name.equals("白金迪斯科")) {
            name = "白金disco";
        }
        if(name.equals("粉红的回忆")){
            name = "粉红色的回忆";
        }
        if (name.equals("大叔别跑")) {
            name = "大叔不要跑";
        }
		if (name.equals("情深深雨蒙蒙")) {
            name = "情深深雨濛濛";
        }
        if(name.equals("爱，一直存在") || name.equals("爱,一直存在")){
            name = "爱一直存在";
        }
        if(name.equals("butterfly") || name.equals("butter fly")){
            name = "butter-fly";
        }
        if(name.equals("都市夜归人")){
            name = "都是夜归人";
        }
        if(name.equals("斯卡布罗集市")){
            name = "scarborough fair";
        }
        if(name.equals("secret base")){
            name = "未闻花名";
        }
        if(name.equals("malaysia chabor")){
            name = "马来西亚的查某";
        }
        if(name.equals("i'll always love you")){
            name = "i will always love you";
        }
        if(name.equals("祝你生日快乐") || name.equals("生日快乐歌") || name.equals("生日歌")){
            name = "生日快乐";
        }
        if(name.equals("可能的夜晚")){
            name = "有可能的夜晚";
        }
        if(name.equals("神话")){
            name = "美丽的神话";
        }
        if(name.equals("safe and sound") || name.equals("safe&sound")){
            name = "safe & sound";
        }
        if(name.equals("fuckin perfect")){
            name = "fuckin' perfect";
        }
        if(name.equals("老子明天不上班")){
            name = "明天不上班";
        }
        if(name.equals("hand clap")){
            name = "handclap";
        }
        if(name.equals("wakawaka")){
            name = "waka waka";
        }
        if(name.equals("穿越时空的爱恋")){
            name = "超越时空的思念";
        }
        if(name.equals("cinderella")){
            name = "辛德瑞拉";
        }
        if(name.equals("爱，很简单")){
            name = "爱很简单";
        }
        if(name.equals("燃烧我的卡路里")){
            name = "卡路里";
        }
        if(name.equals("百万个可能")){
            name = "一百万个可能";
        }
		if(name.equals("7月7日晴")){
			name = "七月七日晴";
		}
		if(name.equals("complucated")){
			name = "complicated";
		}
		if(name.equals("stutteering") || name.equals("stutteeing")){
			name = "stuttering";
		}
		if(name.equals("你，好不好？") ||name.equals("你，好不好?")){
			name = "你，好不好";
		}
		if(name.equals("solider")){
			name = "soldier";
		}
		if(name.equals("falling")){
			name = "下坠falling";
		}
		if(name.equals("some just like this")){
			name = "something just like this";
		}
		if(name.equals("god is a gril")){
			name = "god is a girl";
		}
		if(name.equals("下个路口见") || name.equals("下个，路口，见")){
			name = "下个,路口,见";
		}
		if(name.equals("即视感")){
			name = "既视感";
		}
		if(name.equals("what you want form me")){
			name = "what you want from me";
		}
		if(name.equals("一个像夏天 一个像秋天")){
			name = "一个像夏天一个像秋天";
		}
		if(name.equals("好的晚安")){
			name = "好的 晚安";
		}
		if(name.equals("狂狼")){
			name = "狂浪";
		}
        return  name;
    }

    static class Song {
        private String name;
        private int times;
        private Date lastTime;

        public Song(String name){
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getTimes() {
            return times;
        }

        public void setTimes(int times) {
            this.times = times;
        }

        public Date getLastTime() {
            return lastTime;
        }

        public void setLastTime(Date lastTime) {
            this.lastTime = lastTime;
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            Song s = (Song)obj;
            if(this.getName().equals(s.getName())){
                return true;
            }
            return false;
        }
    }

    static class Record {
        private int seq;
        private String name;

        public Record(String name){
            this.name = name;
        }

        public Record(int seq, String name, Date time) {
            this.seq = seq;
            this.name = name;
            this.time = time;
        }

        public int getSeq() {
            return seq;
        }

        public void setSeq(int seq) {
            this.seq = seq;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getTime() {
            return time;
        }

        public void setTime(Date time) {
            this.time = time;
        }

        private Date time;

        @Override
        public boolean equals(Object obj) {
            Record r = (Record)obj;
            if(this.getName().equals(r.getName())){
                return true;
            }
            return false;
        }
    }
}
