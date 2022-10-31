package tide.trader.bot.service;

import tide.trader.bot.dto.util.ColumnsDTO;

public class Test {
    public static void main(String[] args) {
        ColumnsDTO metaColumn = new ColumnsDTO("title");
        String dataTabs = metaColumn.setColName("aaa", "bbb", "ccc").addRow("测试1", "测试2", "测试3").dataTabs();
        System.out.println(dataTabs);
    }
}
