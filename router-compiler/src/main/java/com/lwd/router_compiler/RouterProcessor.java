package com.lwd.router_compiler;

import com.google.auto.service.AutoService;
import com.lwd.router_annotations.Router;
import com.lwd.router_annotations.bean.RouterBean;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.lang.reflect.Type;
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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


@AutoService(Processor.class)
@SupportedAnnotationTypes({ProcessorConfig.ROUTER_PACKAGE})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("moduleName")
public class RouterProcessor extends AbstractProcessor {

    //操作element的工具
    private Elements elementsTool;

    //操作类信息的工具
    private Types typeTool;

    //打印
    private Messager messager;

    //文件生成器
    private Filer filer;
    private String mModuleName;
    private String aptPackage;

    private Map<String, List<RouterBean>> pathMap = new HashMap<>();//（group，类集合）
    private Map<String, String> groupMap = new HashMap<>();//group，pathfile路径

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        elementsTool = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        typeTool = processingEnvironment.getTypeUtils();

        Map<String, String> options = processingEnvironment.getOptions();
        mModuleName = options.get(ProcessorConfig.MODULE_NAME);
        aptPackage = options.get(ProcessorConfig.APT_PACKAGE);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>" + mModuleName + "<<<<<<<<<<<<<<<");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>" + "run" + "<<<<<<");

//        package com.example.helloworld;
//
//        public final class HelloWorld {
//            public static void main(String[] args) {
//                System.out.println("Hello, JavaPoet!");
//            }
//        }
        /*
        //1 方法
        MethodSpec method =
                MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(String[].class, "args")
                .addCode("$T.out.println($S);", System.class, "Hello, JavaPoet!")
                .build();

        //2 类
        TypeSpec typeSpec = TypeSpec.classBuilder("HelloWorld")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(method)
                .build();

        //3 包
        JavaFile javaFile = JavaFile.builder("com.example.helloworld", typeSpec)
                .build();

        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>" + "文件生成失败" + "<<<<<<");
            return false;
        }
        */
        if (set == null || set.size() == 0) {
            return false;
        }
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Router.class);

        TypeElement activityType = elementsTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
        TypeMirror activityMirror = activityType.asType();

        for (Element element : elements) {
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, "被Router注解的类：" + className);

            Router router = element.getAnnotation(Router.class);
            /*
            //1 方法
            MethodSpec findTargetClass = MethodSpec.methodBuilder("findTargetClass")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(Class.class)
                    .addParameter(String.class, "path")
                    .addStatement("return path.equals($S) ? $T.class : null",
                            router.path(), ClassName.get((TypeElement) element))
                    .build();
            //2 类
            TypeSpec typeSpec = TypeSpec.classBuilder(className + mModuleName + "$$Router" )
                    .addMethod(findTargetClass)
                    .addModifiers(Modifier.PUBLIC)
                    .build();
            //3 包
            String pkg = elementsTool.getPackageOf(element).getQualifiedName().toString();
            JavaFile javaFile = JavaFile.builder(pkg, typeSpec)
                    .build();

            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }*/

            RouterBean routerBean = new RouterBean.Builder()
                    .addGroup(router.group())
                    .addPath(router.path())
                    .addElement(element)
                    .build();
            messager.printMessage(Diagnostic.Kind.NOTE, routerBean.toString());

            TypeMirror elementMirror = element.asType();
            boolean isActivity = typeTool.isSubtype(elementMirror, activityMirror);
            if (isActivity) {
                routerBean.setTypeEnum(RouterBean.TypeEnum.ACTIVITY);
            } else {
                messager.printMessage(Diagnostic.Kind.NOTE, "被Router注解的类型未适配：" + className);
                return false;
            }

            if (check(routerBean)) {
                messager.printMessage(Diagnostic.Kind.NOTE, "check success:" + className);
                List<RouterBean> routerBeans = pathMap.get(routerBean.getGroup());
                if (ProcessorUtils.isEmpty(routerBeans)) {
                    routerBeans = new ArrayList<>();
                    routerBeans.add(routerBean);
                    pathMap.put(routerBean.getGroup(), routerBeans);
                } else {
                    routerBeans.add(routerBean);
                }
            } else {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "Router注解的路径不正确:" + routerBean.getPath());
                return false;
            }
        }
        TypeElement pathType = elementsTool.getTypeElement(ProcessorConfig.AROUTER_API_PATH);
        TypeElement groupType = elementsTool.getTypeElement(ProcessorConfig.AROUTER_API_GROUP);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>" + pathType + "<<<<<<");


        try {
            createPathFile(pathType);
        } catch (Exception e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>" + e.getStackTrace() + "<<<<<<");

        }

        createGroupFile(groupType, pathType);


        return true;
    }

    private void createGroupFile(TypeElement groupType, TypeElement pathType) {
        /*public class ARouter$$Group$$personal implements ARouterGroup {
            @Override
            public Map<String, Class<? extends ARouterPath>> getGroupMap() {
                Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
                groupMap.put("personal", ARouter$$Path$$personal.class);
                return groupMap;
            }
        }*/
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))));

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.GROUP_METHOD_NAME)
                .returns(parameterizedTypeName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class);

        methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))),
                ProcessorConfig.GROUP_VAR1,
                ClassName.get(HashMap.class));

        for (Map.Entry<String, String> entry : groupMap.entrySet()) {
            methodBuilder.addStatement("$N.put($S, $T.class)",
                    ProcessorConfig.GROUP_VAR1,
                    entry.getKey(),
                    ClassName.get(aptPackage, entry.getValue()));
        }

        methodBuilder.addStatement("return $N", ProcessorConfig.GROUP_VAR1);

        String finalName = ProcessorConfig.GROUP_FILE_NAME + mModuleName;

        TypeSpec typeSpec = TypeSpec.classBuilder(finalName)
                .addSuperinterface(ClassName.get(groupType))
                .addMethod(methodBuilder.build())
                .addModifiers(Modifier.PUBLIC)
                .build();

        try {
            JavaFile.builder(aptPackage, typeSpec).build().writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>" + "文件生成失败" + "<<<<<<");
        }
    }

    private void createPathFile(TypeElement pathType) throws Exception {
        /*public class ARouter$$Path$$personal implements ARouterPath {
            @Override
            public Map<String, RouterBean> getPathMap() {
                Map<String, RouterBean> pathMap = new HashMap<>();
                pathMap.put("/personal/Personal_Main2Activity", RouterBean.create();
                pathMap.put("/personal/Personal_MainActivity", RouterBean.create());
                return pathMap;
            }
        }*/
        ParameterizedTypeName returnMethod = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouterBean.class));

        for (Map.Entry<String, List<RouterBean>> entry : pathMap.entrySet()) {

            MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(ProcessorConfig.PATH_METHOD_NAME)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(returnMethod)
                    .addStatement("$T<$T, $T> $N = new $T<>()",
                            ClassName.get(Map.class),
                            ClassName.get(String.class),
                            ClassName.get(RouterBean.class),
                            ProcessorConfig.PATH_VAR1,
                            ClassName.get(HashMap.class));

            for (RouterBean routerBean : entry.getValue()) {
                methodSpec.addStatement("$N.put($S, $T.create($T.$L, $T.class, $S, $S))",
                        ProcessorConfig.PATH_VAR1,
                        routerBean.getPath(),
                        ClassName.get(RouterBean.class),
                        ClassName.get(RouterBean.TypeEnum.class),
                        routerBean.getTypeEnum(),
                        ClassName.get((TypeElement) routerBean.getElement()),
                        routerBean.getPath(),
                        routerBean.getGroup());
            }

            methodSpec.addStatement("return $N", ProcessorConfig.PATH_VAR1);

            String finalName = ProcessorConfig.PATH_FILE_NAME + entry.getKey();


            TypeSpec typeSpec = TypeSpec.classBuilder(finalName)
                    .addSuperinterface(ClassName.get(pathType))
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(methodSpec.build())
                    .build();


                JavaFile.builder(aptPackage, typeSpec).build().writeTo(filer);

            groupMap.put(entry.getKey(), finalName);
        }
    }

    private boolean check(RouterBean bean) {
            String path = bean.getPath();
            String group = bean.getGroup();

            if (ProcessorUtils.isEmpty(path) || !path.startsWith("/")) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@Router路径必须以/开头");
                return false;
            }

            if (path.lastIndexOf("/") == 0) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@Router路径不能以/结尾");
                return false;
            }

            String finalGroup = path.substring(1, path.indexOf("/", 1));
            bean.setGroup(finalGroup);

        return true;
    }
}