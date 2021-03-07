package com.lwd.router_compiler;

import com.google.auto.service.AutoService;
import com.lwd.router_annotations.Parameter;
import com.lwd.router_annotations.bean.RouterBean;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * @AUTHOR lianwd
 * @TIME 3/6/21
 * @DESCRIPTION TODO
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({ProcessorConfig.PARAMETER_PACKAGE})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ParameterProcessor extends AbstractProcessor {

    //操作element的工具
    private Elements elementsTool;

    //操作类信息的工具
    private Types typeTool;

    //打印
    private Messager messager;

    //文件生成器
    private Filer filer;

    private Map<TypeElement, List<Element>> parameterMap = new HashMap<>();//（activity，被注解的元素集合）

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        elementsTool = processingEnvironment.getElementUtils();
        typeTool = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (ProcessorUtils.isEmpty(set)) {
            return false;
        }
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Parameter.class);
        messager.printMessage(Diagnostic.Kind.NOTE, "被@Parameter注解的参数个数" + elements.size());
        if (elements != null) {
            for (Element element : elements) {
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                //加入缓存
                if (parameterMap.containsKey(enclosingElement)) {
                    parameterMap.get(enclosingElement).add(element);
                } else {
                    List<Element> list = new ArrayList<>();
                    list.add(element);
                    parameterMap.put(enclosingElement, list);
                }
            }

            TypeElement activityType = elementsTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
            TypeElement parameterType = elementsTool.getTypeElement(ProcessorConfig.AROUTER_AIP_PARAMETER_GET);

            ParameterSpec parameterSpec = ParameterSpec.builder(
                    TypeName.OBJECT, ProcessorConfig.PARAMETER_NAME).build();

            for (Map.Entry<TypeElement, List<Element>> entry : parameterMap.entrySet()) {
                //被@Parameter注解的参数的类
                TypeElement typeElement = entry.getKey();

                if (!typeTool.isSubtype(typeElement.asType(), activityType.asType())) {
                    throw new RuntimeException("@Parameter注解只能作用于activity上");
                }

                ClassName className = ClassName.get(typeElement);
                ParameterFactory factory = new ParameterFactory.Builder()
                        .setMessager(messager)
                        .setClassName(className)
                        .setParameterSpec(parameterSpec)
                        .build();

                factory.addFirstStatement();

                for (Element element : entry.getValue()) {
                    factory.buildStatement(element);
                }

                String finalClassName = typeElement.getSimpleName() + ProcessorConfig.PARAMETER_FILE_NAME;
                messager.printMessage(Diagnostic.Kind.NOTE, "APT生成获取参数类文件：" +
                        className.packageName() + "." + finalClassName);

                TypeSpec typeSpec = TypeSpec.classBuilder(finalClassName)
                        .addSuperinterface(ClassName.get(parameterType))
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(factory.methodBuild())
                        .build();

                try {
                    JavaFile.builder(className.packageName(), typeSpec).build().writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return true;
    }
}
