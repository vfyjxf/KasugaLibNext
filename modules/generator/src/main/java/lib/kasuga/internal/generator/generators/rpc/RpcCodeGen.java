package lib.kasuga.internal.generator.generators.rpc;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import lib.kasuga.internal.generator.CodeGenerator;

import java.util.List;
import java.util.Objects;

public class RpcCodeGen implements CodeGenerator {
    private static final int RPC_MAX_PARAM = 10;
    public CompilationUnit generate(CompilationUnit cu) {
        ClassOrInterfaceDeclaration root = cu.getPrimaryType().orElseThrow().asClassOrInterfaceDeclaration();
        List<BodyDeclaration<?>> originalMember = List.copyOf(root.getMembers());
        for (BodyDeclaration<?> member : originalMember) {
            if(member.isClassOrInterfaceDeclaration()) {
                root.remove(member);
                processMember(root, member.asClassOrInterfaceDeclaration());
            }
        }
        return cu;
    }

    private static void processMember(ClassOrInterfaceDeclaration root, ClassOrInterfaceDeclaration member) {
        if(Objects.equals(member.getNameAsString(), "TemplateV")) {
            processMember("RpcV", root, member, false);
        } else if(Objects.equals(member.getNameAsString(), "TemplateR")) {
            processMember("RpcR", root, member, true);
        }
    }

    private static void processMember(String prefix, ClassOrInterfaceDeclaration root, ClassOrInterfaceDeclaration member, boolean isReturnable) {
        for (int i = 0; i < RPC_MAX_PARAM + 1; i++) {
            ClassOrInterfaceDeclaration memberPrototype = member.clone();
            memberPrototype.setName(prefix + i);
            root.addMember(memberPrototype);
            int finalI = i;

            NodeList<TypeParameter> typeParameters = memberPrototype.getTypeParameters();
            for(int j=1;j<=i;j++) {
                typeParameters.add(new TypeParameter("T" + j));
            }

            memberPrototype
                    .getExtendedTypes(0)
                    .getTypeArguments()
                    .flatMap(NodeList::getFirst)
                    .ifPresent(q -> q.ifClassOrInterfaceType(t -> t.getScope().ifPresent(s->{
                        s.setName(prefix + finalI);
                        NodeList<Type> typeArgs = s.getTypeArguments().orElse(new NodeList<>());
                        for (int j = 1; j <= finalI; j++) {
                            typeArgs.add(new TypeParameter("T" + j));
                        }
                        s.setTypeArguments(typeArgs);
                        cleanUpTypeArgumentTag(s);
                    })));

            memberPrototype
                    .getExtendedTypes(0)
                    .getTypeArguments()
                    .flatMap(NodeList::getLast)
                    .ifPresent(q -> q.ifClassOrInterfaceType(t -> {
                        t.setName(prefix + finalI);
                        NodeList<Type> typeArgs = t.getTypeArguments().orElse(new NodeList<>());
                        for (int j = 1; j <= finalI; j++) {
                            typeArgs.add(new TypeParameter("T" + j));
                        }
                        t.setTypeArguments(typeArgs);
                        cleanUpTypeArgumentTag(t);
                    }));



            for (ConstructorDeclaration constructor : memberPrototype.getConstructors()) {
                constructor.setName(prefix + i);
            }



            memberPrototype.setTypeParameters(typeParameters);

            for (ConstructorDeclaration constructor : memberPrototype.getConstructors()) {
                NodeList<Parameter> parameters = constructor.getParameters();
                for (Parameter parameter : parameters) {
                    if(parameter.getType().isClassOrInterfaceType() && Objects.equals(parameter.getType().asClassOrInterfaceType().getNameAsString(), "RpcFunction")) {
                        NodeList<Type> typeArgs = parameter.getType().asClassOrInterfaceType().getTypeArguments().orElse(new NodeList<>());
                        for (int j = 1; j <= finalI; j++) {
                            typeArgs.add(new TypeParameter("T" + j));
                        }
                        parameter.getType().asClassOrInterfaceType().setTypeArguments(typeArgs);
                        cleanUpTypeArgumentTag(parameter.getType().asClassOrInterfaceType());
                    }
                }
                for (int j = 1; j <= i; j++) {
                    String name = "c" + j;
                    ClassOrInterfaceType type = new ClassOrInterfaceType();
                    type.setName("StreamCodec");
                    type.setTypeArguments(new TypeParameter("? super FriendlyByteBuf"), new TypeParameter("T" + j));
                    cleanUpTypeArgumentTag(type);
                    parameters.add(new Parameter(type, name));
                }

                constructor.getBody().accept(new ModifierVisitor<NodeList<TypeParameter>>(){
                    @Override
                    public Node visit(ImportDeclaration n, NodeList<TypeParameter> arg) {
                        return super.visit(n, arg);
                    }
                }, typeParameters);
            }

            for (BodyDeclaration<?> memberPrototypeMember : memberPrototype.getMembers()) {
                if(!memberPrototypeMember.isClassOrInterfaceDeclaration()) {
                    continue;
                }
                switch (memberPrototypeMember.asClassOrInterfaceDeclaration().getNameAsString()) {
                    case "Request":
                        ClassOrInterfaceDeclaration requestClass = memberPrototypeMember.asClassOrInterfaceDeclaration();
                        ConstructorDeclaration constructorDeclaration = requestClass.getConstructors().getFirst();
                        for (int j = 1; j <= i; j++) {
                            String typeName = "T" + j;
                            requestClass.getMembers().add(new FieldDeclaration(new NodeList<>(
                                    new Modifier(Modifier.Keyword.PUBLIC)
                            ), new VariableDeclarator(new TypeParameter(typeName), "p" + j)));
                            constructorDeclaration.getParameters().add(new Parameter(new TypeParameter(typeName), "p" + j));
                            constructorDeclaration.getBody().addStatement("this.p" + j + " = p" + j + ";");
                        }
                        break;
                    case "RpcFunction":
                        ClassOrInterfaceDeclaration functionInterface = memberPrototypeMember.asClassOrInterfaceDeclaration();
                        List<MethodDeclaration> methods = functionInterface.getMethods();
                        NodeList<TypeParameter> typeParameters1 = new NodeList<>();
                        for (int j = 1; j <= i; j++) {
                            typeParameters1.add(new TypeParameter("T" + j));
                        }
                        functionInterface.getTypeParameters().addAll(typeParameters1);
                        for (MethodDeclaration method : methods) {
                            NodeList<Parameter> parameters = method.getParameters();
                            for (int j = 1; j <= i; j++) {
                                parameters.add(new Parameter(new TypeParameter("T" + j), "p" + j));
                            }
                            method.setParameters(parameters);
                            method.getBody().ifPresent(body->{
                                body.accept(new ModifierVisitor<>(){
                                    @Override
                                    public Visitable visit(MethodCallExpr n, Object arg) {
                                        if(n.getNameAsString().equals("call")){
                                            NodeList<Expression> args = n.getArguments();
                                            for (int j = 1; j <= finalI; j++) {
                                                args.add(new NameExpr("p" + j));
                                            }
                                            n.setArguments(args);
                                        }
                                        return n;
                                    }
                                }, null);
                            });
                        }
                        break;
                }
            }

            for (BodyDeclaration<?> memberPrototypeMember : memberPrototype.getMembers()) {
                if(memberPrototypeMember.isFieldDeclaration()) {
                    FieldDeclaration field = memberPrototypeMember.asFieldDeclaration();
                    for (VariableDeclarator variable : field.getVariables()) {
                        if(variable.getType().isClassOrInterfaceType() && variable.getType().asClassOrInterfaceType().getNameAsString().equals("RpcFunction")){
                            NodeList<Type> typeArgs = variable.getType().asClassOrInterfaceType().getTypeArguments().orElse(new NodeList<>());
                            for (int j = 1; j <= finalI; j++) {
                                typeArgs.add(new TypeParameter("T" + j));
                            }
                            variable.getType().asClassOrInterfaceType().setTypeArguments(typeArgs);
                            cleanUpTypeArgumentTag(variable.getType().asClassOrInterfaceType());
                        }
                    }
                }
            }

            NodeList<Statement> stmt = memberPrototype.getConstructors().getFirst().getBody().getStatements();
            for (Statement statement : stmt) {
                if(!statement.isExpressionStmt())
                    continue;

                Expression expr = statement.asExpressionStmt().getExpression();
                if(!expr.isAssignExpr())
                    continue;

                Expression target = expr.asAssignExpr().getTarget();

                if(!target.isFieldAccessExpr() || !target.asFieldAccessExpr().getScope().isThisExpr()) {
                    continue;
                }

                SimpleName targetName = target.asFieldAccessExpr().getName();

                if(!Objects.equals(targetName.getIdentifier(), "codec")){
                    continue;
                }

                Expression value = expr.asAssignExpr().getValue();
                NodeList<BodyDeclaration<?>> body = value.asObjectCreationExpr().getAnonymousClassBody().orElseThrow();
                for (BodyDeclaration<?> bodyDeclaration : body) {
                    if(!bodyDeclaration.isMethodDeclaration())
                        continue;
                    MethodDeclaration methodDeclaration = bodyDeclaration.asMethodDeclaration();
                    if(Objects.equals(methodDeclaration.getNameAsString(), "encode")) {
                        NodeList<Statement> statements = new NodeList<>();
                        for (int j = 1; j <=i ; j++) {
                            statements.add(StaticJavaParser.parseStatement("c"+j+".encode(o, request.p"+j+");"));
                        }
                        methodDeclaration.getBody().ifPresent(b->b.setStatements(statements));
                    } else if(Objects.equals(methodDeclaration.getNameAsString(), "decode")) {
                        NodeList<Statement> statements = new NodeList<>();
                        ObjectCreationExpr expr1 = new ObjectCreationExpr();
                        expr1.setType("Request");
                        for(int j = 1;j<=i;j++){
                            expr1.addArgument("c" + j + ".decode(byteBuf)");
                        }
                        statements.add(new ReturnStmt(expr1));
                        methodDeclaration.getBody().ifPresent(b->b.setStatements(statements));
                    }
                }
            }

            for (BodyDeclaration<?> memberPrototypeMember : memberPrototype.getMembers()) {
                if(!memberPrototypeMember.isMethodDeclaration()) {
                    continue;
                }
                MethodDeclaration methodDeclaration = memberPrototypeMember.asMethodDeclaration();
                switch (methodDeclaration.getNameAsString()) {
                    case "call":
                        NodeList<Parameter> parameters = methodDeclaration.getParameters();
                        for (int j = 1; j <= i; j++) {
                            parameters.add(new Parameter(new TypeParameter("T" + j), "p" + j));
                        }
                        BlockStmt body = methodDeclaration.getBody().orElseThrow();
                        body.accept(new ModifierVisitor<>(){
                            @Override
                            public Visitable visit(ObjectCreationExpr n, Object arg) {
                                if(n.getType().getNameAsString().equals("Request")){
                                    for (int j = 0; j < finalI; j++) {
                                        n.addArgument(new NameExpr("p" + (j + 1)));
                                    }
                                }
                                return n;
                            }
                        }, null);
                        break;
                    case "handleInstant":
                        BlockStmt handleInstantBody = methodDeclaration.getBody().orElseThrow();
                        handleInstantBody.accept(new ModifierVisitor<>(){
                            @Override
                            public Visitable visit(MethodCallExpr n, Object arg) {
                                if(n.getNameAsString().equals("call")){
                                    NodeList<Expression> args = n.getArguments();
                                    for (int j = 1; j <= finalI; j++) {
                                        args.add(new NameExpr("request.p" + j));
                                    }
                                    n.setArguments(args);
                                }
                                return n;
                            }
                        }, null);
                        break;
                }
            }
        }

        for(int i=0;i<RPC_MAX_PARAM + 1;i++) {
            // Rpc.wrap() -> new Rpc.Vi()


            MethodDeclaration wrapMethod = new MethodDeclaration();
            wrapMethod.setName("wrap");
            ClassOrInterfaceType returnType = new ClassOrInterfaceType();
            NodeList<Type> types = new NodeList<>();
            NodeList<TypeParameter> typeParameters = new NodeList<>();
            returnType.setName(prefix + i);

            if(isReturnable) {
                types.add(new TypeParameter("R"));
                typeParameters.add(new TypeParameter("R"));
            }
            for(int j=1;j<=i;j++){
                types.add(new TypeParameter("T" + j));
                typeParameters.add(new TypeParameter("T" + j));
            }

            if(!types.isEmpty()){
                returnType.setTypeArguments(types);
                wrapMethod.setTypeParameters(typeParameters);
            }

            wrapMethod.setType(returnType);

            wrapMethod.setModifiers(Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
            wrapMethod.addParameter("String", "name");
            ClassOrInterfaceType funcType = new ClassOrInterfaceType();
            funcType.setName("RpcFunction");
            funcType.setScope(new ClassOrInterfaceType(prefix + i));
            if(!typeParameters.isEmpty()){
                funcType.setTypeArguments(types);
            }
            wrapMethod.addParameter(funcType, "func");
            if(isReturnable) {
                wrapMethod.addParameter(new Parameter(new TypeParameter("StreamCodec<? super FriendlyByteBuf,R>"), "r"));
            }
            for(int j=1;j<=i;j++){
                wrapMethod.addParameter(new Parameter(new TypeParameter("StreamCodec<? super FriendlyByteBuf,T"+j+">"), "c"+j));
            }

            BlockStmt body = new BlockStmt();
            ObjectCreationExpr newExpr = new ObjectCreationExpr();
            ClassOrInterfaceType newType = new ClassOrInterfaceType();
            newType.setName(prefix + i);

            if(!types.isEmpty()){
                newType.setTypeArguments(types);
            }
            newExpr.setType(newType);
            newExpr.addArgument("name");
            newExpr.addArgument("func");
            if(isReturnable) {
                newExpr.addArgument("r");
            }
            for(int j=1;j<=i;j++){
                newExpr.addArgument("c" + j);
            }
            body.addStatement(new ReturnStmt(newExpr));
            wrapMethod.setBody(body);
            root.addMember(wrapMethod);
        }
    }

    private static void cleanUpTypeArgumentTag(ClassOrInterfaceType s) {
        if(s.getTypeArguments().map(NodeList::isEmpty).orElse(false))
            s.removeTypeArguments();
    }
}
