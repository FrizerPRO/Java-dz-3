package ru.hse.jade.sample.annotation_setup;

import jdk.jfr.SettingControl;
import ru.hse.jade.sample.agents.TestAgent;
import ru.hse.jade.sample.configuration.JadeAgent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Proxy;
import java.util.Map;


public interface SetAnnotationNumber {
    default void setNumber(int number){
        try {
            final JadeAgent oldAnnotation = (JadeAgent) getClass().getAnnotations()[0];
            Annotation newAnnotation = new JadeAgent() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return oldAnnotation.annotationType();
                }
                @Override
                public int number() {
                    return number;
                }
                @Override
                public String value() {
                    return "";
                }
            };
            AnnotationHelper.alterAnnotationOn(getClass(), JadeAgent.class,newAnnotation);
        } catch (Exception ex){
            System.out.println("Can't set number of agents.");
        }
    }

}
