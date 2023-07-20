package mixin.extra.dump;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class BytecodeWriter {
	protected static final HashMap<Integer, String> INSN_NAMES = new HashMap<>();
	
	static {
		boolean hitOps = false;
		for (Field declaredField : Opcodes.class.getDeclaredFields()) {
			if (!hitOps && declaredField.getName().equals("NOP"))
				hitOps = true;
			
			if (hitOps) {
				try {
					Integer i = (Integer) declaredField.get(null);
					INSN_NAMES.put(i, declaredField.getName().toLowerCase());
				} catch (Throwable ignored) {
				}
			}
		}
	}
	
	protected static String getVarName(int id, MethodNode method) {
		if (method == null) return String.valueOf(id);
		
		if (method.localVariables != null)
			for (LocalVariableNode localVariable : method.localVariables)
				if (localVariable.index == id)
					return localVariable.name;
		
		return String.valueOf(id);
	}
	
	protected static String getLabelName(HashMap<Label, String> map, Label lbl) {
		String text = map.get(lbl);
		if (text == null) return lbl.toString();
		return text;
	}
	
	public static String toString(MethodNode method, AbstractInsnNode insnNode, HashMap<Label, String> labels) {
		if (insnNode instanceof FrameNode) return null;
		
		if (insnNode instanceof LabelNode) return getLabelName(labels, ((LabelNode) insnNode).getLabel());
		if (insnNode instanceof LineNumberNode ln) return "line " + ln.line + " " + getLabelName(labels, ln.start.getLabel());
		
		String name = INSN_NAMES.get(insnNode.getOpcode());
		int[] stackChange = StackTracker.getIO(insnNode);
		String text = "\t" + name;
		
		String total = String.valueOf(stackChange[0] - stackChange[1]);
		if (total.equals("0") || total.equals("-0")) total = "0";
		else if (!total.startsWith("-")) total = "+" + total;
		
		text = "\t# stack: +" + stackChange[0] + "-" + stackChange[1] + " = " + total + "\n" +
				"\t\t" + text;
		
		if (name == null)
			System.err.println(insnNode.getOpcode() + " has a null name.");
		
		if (insnNode instanceof IntInsnNode)
			return text + " " + ((IntInsnNode) insnNode).operand;
		if (insnNode instanceof InsnNode)
			return text;
		
		if (insnNode instanceof MethodInsnNode) {
			MethodInsnNode insn = (MethodInsnNode) insnNode;
			return text + " " + (insn.itf ? "interface " : "") + insn.owner + "#" + insn.name + insn.desc;
		}
		if (insnNode instanceof FieldInsnNode) {
			FieldInsnNode insn = (FieldInsnNode) insnNode;
			return text + " " + insn.owner + " " + insn.name + " " + insn.desc;
		}
		if (insnNode instanceof IincInsnNode) {
			IincInsnNode insn = (IincInsnNode) insnNode;
			return text + " " + getVarName(insn.var, method) + " += " + insn.incr;
		}
		if (insnNode instanceof VarInsnNode)
			return text + " " + getVarName(((VarInsnNode) insnNode).var, method);
		if (insnNode instanceof JumpInsnNode)
			return text + " " + getLabelName(labels, ((JumpInsnNode) insnNode).label.getLabel());
		if (insnNode instanceof TypeInsnNode)
			return text + " " + ((TypeInsnNode) insnNode).desc;
		
		if (insnNode instanceof LdcInsnNode) {
			LdcInsnNode insn = (LdcInsnNode) insnNode;
			// code
			String typ = "";
			if (insn.cst.getClass().equals(String.class))
				typ = "string ";
			else if (insn.cst.getClass().equals(Integer.class))
				typ = "int ";
			else if (insn.cst.getClass().equals(Byte.class))
				typ = "byte ";
			else if (insn.cst.getClass().equals(Short.class))
				typ = "short ";
			else if (insn.cst.getClass().equals(Long.class))
				typ = "long ";
			else if (insn.cst.getClass().equals(Float.class))
				typ = "float ";
			else if (insn.cst.getClass().equals(Double.class))
				typ = "double ";
			return text + " " + typ + insn.cst;
		}
		
		if (insnNode instanceof TableSwitchInsnNode) {
			TableSwitchInsnNode insn = (TableSwitchInsnNode) insnNode;
			// code
			StringBuilder labelList = new StringBuilder();
			for (int i = 0; i < insn.labels.size() - 1; i++)
				labelList.append(getLabelName(labels, insn.labels.get(i).getLabel())).append(", ");
			labelList.append(getLabelName(labels, insn.labels.get(insn.labels.size() - 1).getLabel()));
			return text + " " + insn.min + ":" + insn.max + " [" + labelList + "] -> " + insn.dflt;
		}
		
		if (insnNode instanceof LookupSwitchInsnNode) {
			LookupSwitchInsnNode insn = (LookupSwitchInsnNode) insnNode;
			// code
			StringBuilder builder = new StringBuilder("[");
			if (!insn.keys.isEmpty()) {
				for (int i = 0; i < insn.keys.size() - 1; i++) {
					Integer key = insn.keys.get(i);
					Object lbl = getLabelName(labels, insn.labels.get(i).getLabel());
					builder.append(key).append("->").append(lbl).append(" ");
				}
				Integer key = insn.keys.get(insn.keys.size() - 1);
				Object lbl = getLabelName(labels, insn.labels.get(insn.labels.size() - 1).getLabel());
				builder.append(key).append("->").append(lbl);
			}
			
			return text + " " + builder + "]->" + insn.dflt;
		}
		
		if (insnNode instanceof InvokeDynamicInsnNode) {
			InvokeDynamicInsnNode insn = (InvokeDynamicInsnNode) insnNode;
			// code
			Handle hndl = null;
			for (Object bsmArg : insn.bsmArgs) {
				if (bsmArg instanceof Handle)
					hndl = (Handle) bsmArg;
			}
			
			if (hndl == null) {
				return text + " " + insn.name + insn.desc;
			}
			
			return text + " " +
					(hndl.isInterface() ? "interface " : "") +
					hndl.getOwner() + " " +
					insn.name + insn.desc + " bsm_handle " +
					hndl.getName() + hndl.getDesc() + hndl.getTag()
					;
		}
		
		System.err.println("Unknown insn type " + insnNode.getClass());
		text += " [unknown]";
		
		return text;
	}
	
	public static void write(ClassNode node, File dst) throws IOException {
		StringBuilder text = new StringBuilder();
		
		text.append(Modifier.toString(node.access)).append(" class \n\textends ").append(node.superName);
		if (!node.interfaces.isEmpty()) {
			text.append(" \n\timplements ");
			for (String anInterface : node.interfaces) text.append(" ").append(anInterface);
		}
		text.append("\n{\n\n");
		
		for (FieldNode field : node.fields) {
			text.append("\t").append(Modifier.toString(field.access))
					.append(field.desc).append(" ").append(field.name)
					.append("\n\t\n");
		}
		
		for (MethodNode method : node.methods) {
			text.append("\t").append(Modifier.toString(method.access))
					.append(" ").append(method.name).append(method.desc)
					.append(" {\n");
			
			HashMap<Label, String> labels = new HashMap<>();
			
			for (AbstractInsnNode instruction : method.instructions) {
				if (instruction instanceof LabelNode lbl) {
					if (!labels.containsKey(lbl.getLabel()))
						labels.put(lbl.getLabel(), "L" + labels.size());
				}
			}
			
			for (AbstractInsnNode instruction : method.instructions) {
				String str = toString(method, instruction, labels);
				if (str == null) continue;
				text.append("\t\t").append(str).append("\n");
			}
			
			if (method.localVariables != null && !method.localVariables.isEmpty()) {
				text.append("\t\t# variables:\n");
				for (LocalVariableNode localVariable : method.localVariables) {
					text.append("\t\t# \t").append(localVariable.index)
							.append(" ").append(localVariable.desc)
							.append(" ").append(localVariable.name)
							.append(" ").append(getLabelName(labels, localVariable.start.getLabel()))
							.append(" ").append(getLabelName(labels, localVariable.end.getLabel()))
							.append("\n")
					;
				}
			}
			
			text.append("\t}\n\t\n");
		}
		
		text.append("}");
		
		FileOutputStream outputStream = new FileOutputStream(dst);
		outputStream.write(text.toString().getBytes(StandardCharsets.UTF_8));
		outputStream.flush();
		outputStream.close();
	}
}
