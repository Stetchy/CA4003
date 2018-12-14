package assignment2;

public class ThreeAddressHandler implements CALParserVisitor {

    private static int labelCount = 1;
    private static int tCount = 1;

    private void operationVisitor(SimpleNode node, Object data) {
        SimpleNode snZero = (SimpleNode) node.jjtGetChild(0);
        SimpleNode snOne = (SimpleNode) node.jjtGetChild(1);
        System.out.println(String.format("\t t%d = %s %s %s", tCount, snZero.value, node.value, snOne.value));
        tCount = tCount + 1;
    }

    @Override
    public Object visit(SimpleNode node, Object data) {
        throw new RuntimeException("Visit SimpleNode");
    }

    @Override
    public Object visit(ASTProg node, Object data) {
        int num = node.jjtGetNumChildren();
        for(int i = 0; i < num; i++) {
            node.jjtGetChild(i).jjtAccept(this, data);
        }
        return data;
    }

    @Override
    public Object visit(ASTFunctionList node, Object data) {
        node.childrenAccept(this, data);
        return node;
    }

    @Override
    public Object visit(ASTVariable node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTConstant node, Object data) {
        SimpleNode snID = (SimpleNode) node.jjtGetChild(0);
        SimpleNode snValue = (SimpleNode) node.jjtGetChild(1).jjtGetChild(1);
        System.out.println("\t" + snID.value + " = " + snValue.value);
        return null;
    }

    @Override
    public Object visit(ASTFunction node, Object data) {
        node.childrenAccept(this, data);
        SimpleNode sn = (SimpleNode) node.jjtGetChild(1);
        System.out.println(sn.value+":");
        return node;
    }

    @Override
    public Object visit(ASTStatementBlock node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTReturn node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTType node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTNempParameterList node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTParameter node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTMain node, Object data) {
        System.out.println("main:");
        int num = node.jjtGetNumChildren();
        for(int i = 0; i < num; i++) {
            node.jjtGetChild(i).jjtAccept(this, data);
        }
        return data;
    }

    @Override
    public Object visit(ASTStatement node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTFunctionCall node, Object data) {
        node.childrenAccept(this, data);
        SimpleNode sn = (SimpleNode) node.jjtGetChild(0);
        System.out.println("\tcall " + sn.value);
        return null;
    }

    @Override
    public Object visit(ASTAssign node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTAdd node, Object data) {
        node.childrenAccept(this, data);
        operationVisitor(node, data);
        return null ;
    }

    @Override
    public Object visit(ASTSubtract node, Object data) {
        node.childrenAccept(this, data);
        operationVisitor(node, data);
        return node;
    }

    @Override
    public Object visit(ASTMinusNum node, Object data) {
        return node;
    }

    @Override
    public Object visit(ASTNum node, Object data) {
        return node;
    }

    @Override
    public Object visit(ASTTrue node, Object data) {
        return node;
    }

    @Override
    public Object visit(ASTFalse node, Object data) {
        return node;
    }

    @Override
    public Object visit(ASTOr node, Object data) {
        node.childrenAccept(this, data);
        return node;
    }

    @Override
    public Object visit(ASTAnd node, Object data) {
        node.childrenAccept(this, data);
        return node;
    }

    @Override
    public Object visit(ASTEqual node, Object data) {
        node.childrenAccept(this, data);
        operationVisitor(node, data);
        return node;
    }

    @Override
    public Object visit(ASTNotEqual node, Object data) {
        node.childrenAccept(this, data);
        operationVisitor(node, data);
        return node;
    }

    @Override
    public Object visit(ASTLessThan node, Object data) {
        node.childrenAccept(this, data);
        operationVisitor(node, data);
        return node;
    }

    @Override
    public Object visit(ASTLessThanEq node, Object data) {
        node.childrenAccept(this, data);
        operationVisitor(node, data);
        return node;
    }

    @Override
    public Object visit(ASTGreaterThan node, Object data) {
        node.childrenAccept(this, data);
        operationVisitor(node, data);
        return node;
    }

    @Override
    public Object visit(ASTGreaterThanEq node, Object data) {
        node.childrenAccept(this, data);
        operationVisitor(node, data);
        return node;
    }

    @Override
    public Object visit(ASTArgList node, Object data) {
        return node;
    }

    @Override
    public Object visit(ASTArg node, Object data) {
        return node;
    }

    @Override
    public Object visit(ASTID node, Object data) {
        return node;
    }
}
