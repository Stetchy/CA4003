package assignment2;

import java.util.*;

public class SemanticCheck implements CALParserVisitor {

    /*****************************************
     ***** Semantic Check Helper Methods *****
     *****************************************/

    private Hashtable<String, LinkedList<STC>> symbolTable = new Hashtable<>();
    private HashMap<String, Object> types = new HashMap<String, Object>() {{
        put("integer", DataType.Integer);
        put("boolean", DataType.Boolean);
    }};
    private String currentScope = "global";
    private HashSet<String> functions = new HashSet<>();
    private HashSet<String> functionCalls = new HashSet<>();
    private HashMap<String, Integer> functionParamNum = new HashMap<>();
    private Hashtable<String, HashSet<String>> varWritten = new Hashtable<>();
    private Hashtable<String, HashSet<String>> varRead = new Hashtable<>();


    private void checkDuplicates() {
        Enumeration e = symbolTable.keys();
        while (e.hasMoreElements()) {
            String scope = (String) e.nextElement();
            ArrayList<String> duplicates = new ArrayList<>();
            ArrayList<String> symbols = new ArrayList<>();
            LinkedList<STC> stc = symbolTable.get(scope);
            for (STC s : stc) {
                if (!symbols.contains(s.getName())) {
                    symbols.add(s.getName());
                } else duplicates.add(s.getName());
            }
            if (duplicates.size() > 0) {
                for (String s : duplicates) {
                    System.out.println("Duplicate declaration " + s + " in scope " + scope);
                }
            }
        }
    }

    private Object getIdType(LinkedList<STC> list, Object nodeValue) {
        Object dataType = DataType.Unknown;
        for (STC stc : list) {
            if (stc.getName().equals(nodeValue.toString())) {
                dataType = stc.getDataType();
            }
        }
        return dataType;
    }

    private void isDeclared(Node node) {
        SimpleNode sn = (SimpleNode) node.jjtGetChild(0);
        LinkedList<STC> list = symbolTable.get(currentScope);
        ArrayList<String> names = new ArrayList<>();
        HashSet<Object> notDeclared = new HashSet<>();
        for (STC stc : list) {
            names.add(stc.getName());
        }
        if (!names.contains(sn.value.toString())) {
            notDeclared.add(sn.value);
        }
        for (Object o : notDeclared) {
            System.out.println(String.format("%s not declared in scope %s", o.toString(), currentScope));
        }
    }

    private void arithOpTypeCheck(SimpleNode node, Object data) {
        if (!((node.jjtGetChild(0).jjtAccept(this, data) == DataType.Integer)
                && (node.jjtGetChild(1).jjtAccept(this, data) == DataType.Integer))) {
            System.out.println("Type error, cannot use binary arithmetic operations on non-integer types.");
        }
    }

    private void boolOpTypeCheck(SimpleNode node, Object data) {
        if (!((node.jjtGetChild(0).jjtAccept(this, data) == DataType.Boolean)
                && (node.jjtGetChild(1).jjtAccept(this, data) == DataType.Boolean))) {
            System.out.println(node.jjtGetChild(1));
            System.out.println("Type error, cannot use boolean operations on non-boolean types.");
        }
    }

    private void sameTypeCheck(SimpleNode node, Object data) {
        Object childZero = node.jjtGetChild(0).jjtAccept(this, data);
        Object childOne = node.jjtGetChild(1).jjtAccept(this, data);
        if (childOne == DataType.Unknown || childZero == DataType.Unknown) {
            System.out.println("Undeclared object found, cannot compare.");
        } else if (!((childZero == DataType.Integer && childOne == DataType.Integer)
                || (childZero == DataType.Boolean && childOne == DataType.Boolean))) {
             System.out.println("Type error, cannot compare different types.");
        }
    }

    private LinkedList<STC> handleSTC(String type, String name, LinkedList<STC> currentScopeST, Object dt) {
        if (currentScopeST == null) {
            currentScopeST = new LinkedList<>();
            STC currentSTC = new STC(name, type, dt);
            currentScopeST.add(currentSTC);
        } else {
            currentScopeST.addFirst(new STC(name, type, dt));
        }
        return currentScopeST;
    }

    private Object getDataType(String objType) {
        if (types.containsKey(objType)) {
            return types.get(objType);
        }
        return DataType.Unknown;
    }

    private void functionDefined(Node node) {
        SimpleNode sn = (SimpleNode) node.jjtGetChild(0);
        String name = sn.value.toString();
        if (!functions.contains(name)) {
            System.out.println(String.format("%s cannot be invoked as it is not defined.", name));
        }
    }

    private void checkType(Node node, Object data) {
        SimpleNode snLeft = (SimpleNode) node.jjtGetChild(0);
        Object snRight;
        if (node.jjtGetChild(1).toString().equals("FunctionCall")) {
            SimpleNode sn = (SimpleNode) node.jjtGetChild(1).jjtGetChild(0);
            snRight = getIdType(symbolTable.get("global"), sn.value);
        } else snRight = node.jjtGetChild(1).jjtAccept(this, data);
        Object leftType = getIdType(symbolTable.get(currentScope), snLeft.value);
        if (!(snRight == leftType) && (snRight != DataType.Unknown)) {
            System.out.println(String.format("%s is of incorrect type, you cannot assign it to the value provided.", snLeft.value));
        }
    }

    private void functionsCalled() {
        ArrayList<String> notCalled = new ArrayList<>();
        for (String s : functions) {
            if (!functionCalls.contains(s)) {
                notCalled.add(s);
            }
        }
        for (String s : notCalled) {
            System.out.println(String.format("Function %s was never called.", s));
        }
    }

    private void checkNumberArgs(Node node) {
        int funcCallArgNum = node.jjtGetChild(1).jjtGetNumChildren();
        SimpleNode sn = (SimpleNode) node.jjtGetChild(0);
        String funcName = sn.value.toString();
        int funcParamNum = functionParamNum.get(funcName);
        if (funcCallArgNum != funcParamNum) {
            System.out.println(String.format("%s arguments supplied to function %s, %s are required.", funcCallArgNum, funcName, funcParamNum));
        }
    }

    private void addToRead(Node node) {
        HashSet<String> list = varRead.get(currentScope);
        if (list == null) {
            list = new HashSet<>();
        }
        int numChildren = node.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            if (node.jjtGetChild(i).toString().equals("ID")) {
                SimpleNode sn = (SimpleNode) node.jjtGetChild(i);
                String name = sn.value.toString();
                list.add(name);
            }
        }
        varRead.put(currentScope, list);
    }

    private void checkRead() {
        Hashtable<String, ArrayList<String>> notRead = varsUsed(varRead);
        Enumeration<String> e = notRead.keys();
        while (e.hasMoreElements()) {
            String scope = e.nextElement();
            for (String s : notRead.get(scope)) {
                System.out.println(String.format("Variable %s is never read in scope %s.", s, scope));
            }
        }
    }

    private void checkWrite() {
        Hashtable<String, ArrayList<String>> notWritten = varsUsed(varWritten);
        Enumeration<String> e = notWritten.keys();
        while (e.hasMoreElements()) {
            String scope = e.nextElement();
            for (String s : notWritten.get(scope)) {
                System.out.println(String.format("Variable %s is never written to in scope %s.", s, scope));
            }
        }
    }

    private Hashtable<String, ArrayList<String>> varsUsed(Hashtable<String, HashSet<String>> varOpSet) {
        Enumeration<String> e = symbolTable.keys();
        Hashtable<String, ArrayList<String>> notUsed = new Hashtable<>();
        while (e.hasMoreElements()) {
            String scope = e.nextElement();
            HashSet<String> used = varOpSet.get(scope);
            ArrayList<String> vars = notUsed.get(scope);
            if (vars == null) {
                vars = new ArrayList<>();
            }
            if (used == null) {
                used = new HashSet<>();
            }
            LinkedList<STC> list = symbolTable.get(scope);
            for (STC stc : list) {
                if (!functions.contains(stc.getName())) {
                    if (!used.contains(stc.getName())) {
                        vars.add(stc.getName());
                    }
                }
            }
            notUsed.put(scope, vars);
        }
        return notUsed;
    }

    /*****************************************
     ***** Node Visitors*****
     *****************************************/

    @Override
    public Object visit(SimpleNode node, Object data) {
        throw new RuntimeException("Visit SimpleNode");
    }

    @Override
    public Object visit(ASTProg node, Object data) {
        int numChildren = node.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            node.jjtGetChild(i).jjtAccept(this, data);
        }
        currentScope = "global";
        symbolTable.computeIfAbsent(currentScope, k -> new LinkedList<>());
        checkDuplicates();
        functionsCalled();
        checkRead();
        checkWrite();
        return DataType.Program;
    }

    @Override
    public Object visit(ASTVariable node, Object data) {
        node.childrenAccept(this, data);
        SimpleNode name = (SimpleNode) node.jjtGetChild(0);
        SimpleNode type = (SimpleNode) node.jjtGetChild(1);
        String varType = (String) type.jjtGetValue();
        String varName = (String) name.jjtGetValue();
        LinkedList<STC> currentScopeST = symbolTable.get(currentScope);
        symbolTable.put(currentScope, handleSTC(varType, varName, currentScopeST, getDataType(varType)));
        return DataType.Variable;
    }

    @Override
    public Object visit(ASTConstant node, Object data) {
        node.childrenAccept(this, data);
        SimpleNode name = (SimpleNode) node.jjtGetChild(0);
        SimpleNode type = (SimpleNode) node.jjtGetChild(1).jjtGetChild(0);
        String constType = (String) type.jjtGetValue();
        String constName = (String) name.jjtGetValue();
        LinkedList<STC> currentScopeST = symbolTable.get(currentScope);
        symbolTable.put(currentScope, handleSTC(constType, constName, currentScopeST, getDataType(constType)));
        HashSet<String> list = varWritten.get(currentScope);
        if (list ==  null) {
            list = new HashSet<>();
            list.add(constName);
        } else list.add(constName);
        varWritten.put(currentScope, list);
        return DataType.Constant;
    }

    @Override
    public Object visit(ASTFunctionList node, Object data) {
        node.childrenAccept(this, data);
        return data;
    }

    @Override
    public Object visit(ASTFunction node, Object data) {
        node.childrenAccept(this, data);
        currentScope = "global";
        SimpleNode name = (SimpleNode) node.jjtGetChild(1);
        SimpleNode type = (SimpleNode) node.jjtGetChild(0);
        String funcType = (String) type.jjtGetValue();
        String funcName = (String) name.jjtGetValue();
        LinkedList<STC> currentScopeST = symbolTable.get(currentScope);
        symbolTable.put(currentScope, handleSTC(funcType, funcName, currentScopeST, getDataType(funcType)));
        functions.add(funcName);
        functionParamNum.put(funcName, node.jjtGetChild(2).jjtGetNumChildren());
        return data;
    }

    @Override
    public Object visit(ASTStatementBlock node, Object data) {
        node.childrenAccept(this, data);
        return data;
    }

    @Override
    public Object visit(ASTReturn node, Object data) {
        node.childrenAccept(this, data);
        addToRead(node);
        return node.value;
    }

    @Override
    public Object visit(ASTType node, Object data) {
        return node.jjtGetValue();
    }

    @Override
    public Object visit(ASTNempParameterList node, Object data) {
        node.childrenAccept(this, data);
        return data;
    }

    @Override
    public Object visit(ASTParameter node, Object data) {
        node.childrenAccept(this, data);
        SimpleNode name = (SimpleNode) node.jjtGetChild(0);
        SimpleNode type = (SimpleNode) node.jjtGetChild(1);
        SimpleNode scope = (SimpleNode) node.jjtGetParent().jjtGetParent().jjtGetChild(1);
        String paramType = (String) type.jjtGetValue();
        String paramName = (String) name.jjtGetValue();
        currentScope = (String) scope.jjtGetValue();
        LinkedList<STC> currentScopeST = symbolTable.get(currentScope);
        currentScopeST = handleSTC(paramType, paramName, currentScopeST, getDataType(paramType));
        symbolTable.put(currentScope, currentScopeST);
        return data;
    }

    @Override
    public Object visit(ASTMain node, Object data) {
        currentScope = "main";
        node.childrenAccept(this, data);
        return data;
    }

    @Override
    public Object visit(ASTStatement node, Object data) {
        node.childrenAccept(this, data);
        HashSet<String> list = varWritten.get(currentScope);
        if (node.jjtGetNumChildren() > 0) {
            if (node.jjtGetChild(0).toString().equals("Assign")) {
                isDeclared(node.jjtGetChild(0));
                checkType(node.jjtGetChild(0), data);
                SimpleNode sn = (SimpleNode) node.jjtGetChild(0).jjtGetChild(0);
                if (list == null) {
                    list = new HashSet<>();
                    list.add(sn.value.toString());
                } else list.add(sn.value.toString());
                varWritten.put(currentScope, list);
                if (node.jjtGetChild(0).jjtGetChild(1).toString().equals("ID")) {
                    HashSet<String> readList = varRead.get(currentScope);
                    SimpleNode readSN = (SimpleNode) node.jjtGetChild(0).jjtGetChild(1);
                    String name = readSN.value.toString();
                    if (readList == null) {
                        readList = new HashSet<>();
                        readList.add(name);
                    } else readList.add(name);
                    varRead.put(currentScope, readList);
                }
            }
        }
        return data;
    }

    @Override
    public Object visit(ASTFunctionCall node, Object data) {
        node.childrenAccept(this, data);
        functionDefined(node);
        checkNumberArgs(node);
        SimpleNode sn = (SimpleNode) node.jjtGetChild(0);
        functionCalls.add(sn.value.toString());
        return getIdType(symbolTable.get(currentScope), node.jjtGetChild(0));
    }

    @Override
    public Object visit(ASTAssign node, Object data) {
        node.childrenAccept(this, data);
        return data;
    }

    @Override
    public Object visit(ASTAdd node, Object data) {
        node.childrenAccept(this, data);
        arithOpTypeCheck(node, data);
        addToRead(node);
        return DataType.Integer;
    }

    @Override
    public Object visit(ASTSubtract node, Object data) {
        node.childrenAccept(this, data);
        arithOpTypeCheck(node, data);
        addToRead(node);
        return DataType.Integer;
    }

    @Override
    public Object visit(ASTMinusNum node, Object data) {
        node.childrenAccept(this, data);
        addToRead(node);
        return DataType.Integer;
    }

    @Override
    public Object visit(ASTNum node, Object data) {
        node.childrenAccept(this, data);
        return DataType.Integer;
    }

    @Override
    public Object visit(ASTTrue node, Object data) {
        node.childrenAccept(this, data);
        return DataType.Boolean;
    }

    @Override
    public Object visit(ASTFalse node, Object data) {
        node.childrenAccept(this, data);
        return DataType.Boolean;
    }

    @Override
    public Object visit(ASTOr node, Object data) {
        node.childrenAccept(this, data);
        boolOpTypeCheck(node, data);
        return node.value;
    }

    @Override
    public Object visit(ASTAnd node, Object data) {
        node.childrenAccept(this, data);
        boolOpTypeCheck(node, data);
        return DataType.Boolean;
    }

    @Override
    public Object visit(ASTEqual node, Object data) {
        node.childrenAccept(this, data);
        sameTypeCheck(node, data);
        addToRead(node);
        return DataType.Boolean;
    }

    @Override
    public Object visit(ASTNotEqual node, Object data) {
        node.childrenAccept(this, data);
        sameTypeCheck(node, data);
        addToRead(node);
        return DataType.Boolean;
    }

    @Override
    public Object visit(ASTLessThan node, Object data) {
        node.childrenAccept(this, data);
        arithOpTypeCheck(node, data);
        addToRead(node);
        return DataType.Boolean;
    }

    @Override
    public Object visit(ASTLessThanEq node, Object data) {
        node.childrenAccept(this, data);
        arithOpTypeCheck(node, data);
        addToRead(node);
        return DataType.Boolean;
    }

    @Override
    public Object visit(ASTGreaterThan node, Object data) {
        node.childrenAccept(this, data);
        arithOpTypeCheck(node, data);
        addToRead(node);
        return DataType.Boolean;
    }

    @Override
    public Object visit(ASTGreaterThanEq node, Object data) {
        node.childrenAccept(this, data);
        arithOpTypeCheck(node, data);
        addToRead(node);
        return DataType.Boolean;
    }

    @Override
    public Object visit(ASTArgList node, Object data) {
        node.childrenAccept(this, data);
        return node.value;
    }

    @Override
    public Object visit(ASTArg node, Object data) {
        node.childrenAccept(this, data);
        isDeclared(node);
        addToRead(node);
        return node.value;
    }

    @Override
    public Object visit(ASTID node, Object data) {
        LinkedList<STC> list = symbolTable.get(currentScope);
        if (list != null) {
            return getIdType(list, node.value);
        } else return node;
    }
}