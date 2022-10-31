package tide.trader.bot.dto.util;

import lombok.Getter;

import java.util.*;

/**
 * 元数据表格
 */

@Getter
public class ColumnsDTO {

    private final String title;
    private final List<String> colNames = new ArrayList<>();
    private final List<List<Object>> datas = new ArrayList<>();

    public ColumnsDTO(String title) {
        this.title = title;
    }

    /**
     * 设置单元格标题
     * @param colName
     * @return
     */
    public ColumnsDTO setColName(String... colName){
        colNames.addAll(Arrays.asList(colName));
        return this;
    }

    /**
     * 添加新列
     * @param value
     * @return
     */
    public ColumnsDTO addRow(Object... value) {
        datas.add(Arrays.asList(value));
        return this;
    }

    /**
     * 添加新列
     * @param value
     * @return
     */
    public ColumnsDTO addReversedRow(Object... value) {
        datas.add(0, Arrays.asList(value));
        return this;
    }

    /**
     * 获取标题
     * @return
     */
    public String title() {
        return title;
    }

    /**
     * 获取表格名
     * @return
     */
    public List<String> colNames() {
        return colNames;
    }

    /**
     * 获取纯数据
     * @return
     */
    public List<List<Object>> datas() {
        return datas;
    }

    /**
     * 获取拼接
     * @return
     */
    public List<List<Object>> tabs() {
        return null;
    }

    /**
     * 数据内容
     * @return
     */
    public String dataTabs(){
        StringBuffer tabBuffer = new StringBuffer();
        for(int i = 0; i< colNames.size(); i++) {
            tabBuffer.append(colNames.get(i));
            if(i + 1 < colNames.size()) {
                tabBuffer.append("\t");
            }
        }
        tabBuffer.append("\n");

        for(int i = 0; i < datas.size(); i++) {
            for(int j = 0; j < datas.get(i).size(); j++) {
                tabBuffer.append(datas.get(i).get(j));
                if(i + 1 < datas.get(i).size()) {
                    tabBuffer.append("\t");
                }
            }

            if(i + 1 < datas.size()) {
                tabBuffer.append("\n");
            }
        }

        return tabBuffer.toString();
    }
}
