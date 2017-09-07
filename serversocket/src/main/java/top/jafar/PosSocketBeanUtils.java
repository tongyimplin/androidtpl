package top.jafar;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import top.jafar.annotation.PosSocketAutowired;

/**
 * 管理工具
 * Created by jafar.new on 2017-9-7.
 */
public class PosSocketBeanUtils {

    private static final Map<String, Object> BEAN_POOL = new HashMap<>();
    private static final Map<String, String> CONFIG_POOL = new HashMap<>();

    /**
     * 设置配置
     * @param key
     * @param val
     */
    public static void setConfig(String key, String val) {
        CONFIG_POOL.put(key, val);
    }

    public static void setConfig(Map<String, String> config) {
        for (Map.Entry<String, String> entry:config.entrySet()) {
            setConfig(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 获取配置
     * @param key
     * @return
     */
    public static String getConfig(String key) {
        return CONFIG_POOL.get(key);
    }

    /**
     * 创建bean
     * @param clazzFullName
     */
    public static Object createBean(String clazzFullName) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        PosSocketLogger.println("创建bean: "+clazzFullName);
        Object instance = null;
        Class<?> aClass = ClassUtils.getClass(clazzFullName);
        instance = aClass.newInstance();
        //注入属性PosSocketAutowired
        Field[] fieldsWithAnnotation = FieldUtils.getFieldsWithAnnotation(aClass, PosSocketAutowired.class);
        for (Field field:fieldsWithAnnotation) {
            String key = field.getType().getName();
            PosSocketAutowired posSocketAutowired = field.getAnnotation(PosSocketAutowired.class);
            String value = posSocketAutowired.value();
            if(StringUtils.isNoneBlank(value)) {
                //如果value不为空表示根据名称注解
                key = value;
            }
            Object val = getBean(key);
            FieldUtils.writeField(field, instance, val, true);
        }
        PosSocketLogger.println("["+clazzFullName+"]创建成功!");
        return instance;
    }

    public static void registerBean(Object ...beans) throws PosSocketException {
        if(beans.length > 0) {
            for (Object bean:beans) {
                if(bean == null) throw new PosSocketException("您注册的bean: "+bean+"为空!");
                String key = bean.getClass().getName();
                PosSocketLogger.println("注册bean: "+key);
                registerBean(key, bean);
            }
        }
    }

    /**
     * 注册bean
     * @param clazzFullName
     * @param bean
     */
    public static void registerBean(String clazzFullName, Object bean) {
        PosSocketLogger.println("注册bean: "+clazzFullName+" => "+bean);
        BEAN_POOL.put(clazzFullName, bean);
    }

    /**
     * 获取bean, 如果没有就新建一个
     * @param name
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name) {
        Object bean = BEAN_POOL.get(name);
        PosSocketLogger.println("获取bean: "+name);
        if(bean == null) {
            synchronized (PosSocketBeanUtils.class) {
                bean = BEAN_POOL.get(name);
                if(bean == null) {
                    try {
                        bean = createBean(name);
                        BEAN_POOL.put(name, bean);
                    }catch (Exception e) {
                        bean = null;
                        e.printStackTrace();
                    }
                }
            }
        }
        PosSocketLogger.println("获取到: "+bean);
        return (T) bean;
    }

}
