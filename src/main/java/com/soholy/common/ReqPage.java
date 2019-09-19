package com.soholy.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * requset请求page对象
 */
@ApiModel(value="分页信息参数",description="分页信息参数" )
public class ReqPage implements Serializable {

    /**
     * 当前页码  默认1
     */
    @ApiModelProperty(example="1",hidden = true)
    private Integer offset = 1;

    /**
     * 页面大小  默认10
     */
    @ApiModelProperty(example="10",dataType = "int",value = "页面大小，默认10")
    private Integer limit = 10;


    /**
     * 页数
     */
    @ApiModelProperty(example="1",dataType = "int",value = "页数，默认1")
    private Integer pageNum = 1;


    public ReqPage(Integer pageNum, Integer limit) {
        this.setPageNum(pageNum);
        this.setLimit(limit);
    }

    public ReqPage() {
    }


    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        if (limit == null || limit < 1) {
            this.limit = 1;
        } else {
            this.limit = limit;
        }
    }

    public Integer getPageNum() {
        return this.getOffset();
    }

    public void setPageNum(Integer pageNum) {
        if (pageNum == null || pageNum < 1) {
            this.pageNum = 1;
        } else {
            this.pageNum = pageNum;
        }
    }

    public Integer getOffset() {
        return (this.pageNum - 1) * this.limit;
    }

    public int getTotalRows(int totalCount) {
        //int totalPageNum = (totalRecord  +  pageSize  - 1) / pageSize;
        return (int) Math.ceil(totalCount / this.limit);
    }

    public static class QReqPage extends ReqPage {

        /**
         * 搜索关键字
         */
        private String keyWord;

        public String getKeyWord() {
            return keyWord;
        }

        public void setKeyWord(String keyWord) {
            if (keyWord != null) {
                keyWord = keyWord.trim();
            }
            this.keyWord = keyWord;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("QReqPage{");
            sb.append("pageNum=").append(super.getPageNum());
            sb.append(", limit=").append(super.getLimit());
            sb.append(", keyWord='").append(keyWord).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ReqPage{");
        sb.append("pageNum=").append(pageNum);
        sb.append(", limit=").append(limit);
        sb.append('}');
        return sb.toString();
    }
}
