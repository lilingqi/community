package com.llq.community.utils;

import com.sun.mail.imap.protocol.ID;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


/**
 * @author llq
 * @create 2021-09-08  11:05
 */
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //敏感词的替换
    private static final String REPLACEMENT = "***";

    //头节点
    private final TrieNode rootNode = new TrieNode();

    //在创建bean之后，使用初始化方法
    //将敏感词加入前缀树
    @PostConstruct
    public void init() {

        try (
                InputStream in = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                //添加进前缀树
                this.addkeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文本失败" + e.getMessage());
        }

    }

    //将一个敏感词添入前缀树
    private void addkeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            Character c = keyword.charAt(i);
            //判断当前字符的节点是否存在
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                subNode = new TrieNode();
                //添加该子节点
                tempNode.addSubNode(c, subNode);
            }
            //进入下一个节点
            tempNode = subNode;
            //设置尾节点标志
            if (i == keyword.length() - 1) {
                tempNode.setEnd(true);
            }
        }
    }

    /**
     * 过滤过程
     *
     * @param text 需要过滤的文本
     * @return 过滤后的结果文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //拼装的结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);

            //跳过符号
            if (isSymbol(c)) {
                //敏感词在中间时直接跳过。只有指针一为根节点的时候才将符号计入结果
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }

            //检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                //begin开头不是敏感词
                sb.append(c);
                //进入下一个位置
                position = ++begin;
                //重置到根节点
                tempNode = rootNode;
            } else if (tempNode.isEnd) {
                //判断为敏感词。添加替换符
                sb.append(REPLACEMENT);
                //进入下一个位置
                begin = ++position;
                //重置到根节点
                tempNode = rootNode;
            } else {
                //检查下一个字符
                position++;
            }
        }
        //将后续不需要处理的结果拼接
        sb.append(text.substring(begin));

        return sb.toString();
    }

    //判断是否为符号
    private boolean isSymbol(Character c) {
        //(c < 0x2E80 || c > 0x9FFF)它们之间的范围为东亚文字范围，所以排除
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //定义前缀树的结构（内部类）
    private class TrieNode {
        //标记是否为结束符
        private boolean isEnd = false;

        //实现树形结构
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        //返回结束标记
        public boolean isEnd() {
            return isEnd;
        }

        //设置结束标记
        public void setEnd(boolean isEnd) {
            this.isEnd = isEnd;
        }

        //添加下级节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        //获取下级节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);

        }

    }
}
