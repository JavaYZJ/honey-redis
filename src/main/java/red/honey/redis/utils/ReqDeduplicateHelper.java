package red.honey.redis.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * @author yangzhijie
 * @date 2020/11/30 10:42
 */
@Slf4j
public class ReqDeduplicateHelper {

    private static String genMD5(String src) {
        String res;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] mdBytes = messageDigest.digest(src.getBytes());
            res = DatatypeConverter.printHexBinary(mdBytes);
        } catch (Exception e) {
            throw new RuntimeException("MD5 hash happen error,the reason :" + e.getMessage());
        }
        return res;
    }

    /**
     * @param reqJSON     请求的参数，这里通常是JSON
     * @param excludeKeys 请求参数里面要去除哪些字段再求摘要
     * @return 去除参数的MD5摘要
     */
    public String deDuplicateParam(final String reqJSON, String... excludeKeys) {
        TreeMap paramTreeMap = JSON.parseObject(reqJSON, TreeMap.class);
        if (excludeKeys != null) {
            List<String> deDuplicateExcludeKeys = Arrays.asList(excludeKeys);
            if (!deDuplicateExcludeKeys.isEmpty()) {
                for (String deDuplicateExcludeKey : deDuplicateExcludeKeys) {
                    paramTreeMap.remove(deDuplicateExcludeKey);
                }
            }
        }
        String paramTreeMapJSON = JSON.toJSONString(paramTreeMap);
        String md5deDupParam = genMD5(paramTreeMapJSON);
        if (log.isDebugEnabled()) {
            log.debug("md5deDupParam = {}, excludeKeys = {} {}", md5deDupParam, Arrays.deepToString(excludeKeys), paramTreeMapJSON);
        }
        return md5deDupParam;
    }
}
