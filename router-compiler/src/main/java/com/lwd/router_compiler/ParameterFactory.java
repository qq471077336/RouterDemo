package com.lwd.router_compiler;

import com.lwd.router_annotations.Parameter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * @AUTHOR lianwd
 * @TIME 3/7/21
 * @DESCRIPTION
 *         @Override
 *         public void getParameter(Object targetParameter) {
 *               Personal_MainActivity t = (Personal_MainActivity) targetParameter;
 *               t.name = t.getIntent().getStringExtra("name");
 *               t.sex = t.getIntent().getStringExtra("sex");
 *         }
 */
public class ParameterFactory {

    private MethodSpec.Builder method;

    private Messager messager;

    //activity
    private ClassName className;

    private ParameterFactory(Builder builder) {
        messager = builder.messager;
        className = builder.className;

        // @Override
        // public void getParameter(Object targetParameter) {
        method = MethodSpec.methodBuilder(ProcessorConfig.PARAMETER_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(builder.parameterSpec);
    }

    /**
     *  Personal_MainActivity t = (Personal_MainActivity) targetParameter;
     */
    public void addFirstStatement() {
        method.addStatement("$T t = ($T)" + ProcessorConfig.PARAMETER_NAME, className, className);
    }

    public MethodSpec methodBuild() {
        return method.build();
    }

    public void buildStatement(Element element) {
        /*
        t.name = t.getIntent().getStringExtra("name");
        t.sex = t.getIntent().getStringExtra("sex");
         */
        TypeMirror typeMirror = element.asType();

        int ordinal = typeMirror.getKind().ordinal();
        String field = element.getSimpleName().toString();
        String fieldValue = "t." + field;
        String methodContent = fieldValue + "= t.getIntent().";
        if (ordinal == TypeKind.INT.ordinal()) {
            methodContent += "getIntExtra($S, " + fieldValue +")";
        } else if (ordinal == TypeKind.BOOLEAN.ordinal()) {
            methodContent += "getBooleanExtra($S, " + fieldValue +")";
        } else {
            if (typeMirror.toString().equalsIgnoreCase(ProcessorConfig.STRING)) {
                methodContent += "getStringExtra($S)";
            }
        }

        String annotation = element.getAnnotation(Parameter.class).name();
        if (ProcessorUtils.isEmpty(annotation)) {
            annotation = field;
        }

        if (methodContent.endsWith(")")) {
            method.addStatement(methodContent, annotation);
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    ">>>>>>>目前仅支持int boolean string<<<<<<<<");
        }
    }

    public static class Builder {
        private Messager messager;

        private ClassName className;

        private ParameterSpec parameterSpec;

        public Messager getMessager() {
            return messager;
        }

        public Builder setMessager(Messager messager) {
            this.messager = messager;
            return this;
        }

        public ClassName getClassName() {
            return className;
        }

        public Builder setClassName(ClassName className) {
            this.className = className;
            return this;
        }

        public ParameterSpec getParameterSpec() {
            return parameterSpec;
        }

        public Builder setParameterSpec(ParameterSpec parameterSpec) {
            this.parameterSpec = parameterSpec;
            return this;
        }

        public ParameterFactory build(){
            if (messager == null) {
                throw new RuntimeException("messager为空");
            }
            if (className == null) {
                throw new RuntimeException("className为空");
            }
            if (parameterSpec == null) {
                throw new RuntimeException("parameterSpec为空");
            }

            return new ParameterFactory(this);
        }
    }
}
