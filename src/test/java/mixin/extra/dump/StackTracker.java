package mixin.extra.dump;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.util.HashMap;

public class StackTracker {
	protected static String getName(AbstractInsnNode insnNode) {
		boolean hitOps = false;
		for (Field field : Opcodes.class.getFields()) {
			if (field.getName().equals("NOP")) hitOps = true;
			if (hitOps) {
				try {
					field.setAccessible(true);
					if (((Integer) field.get(null)) == insnNode.getOpcode()) {
						return field.getName();
					}
				} catch (Throwable ignored) {
				}
			}
		}
		return null;
	}
	
	// extracted for readability
	protected static int descInput(String desc) {
		int count = 0;
		boolean inType = false;
		for (char c : desc.toCharArray()) {
			//@formatter:off
			if (c == '(') continue; // start of desc
			else if (c == ')') break; // terminator for parameters in a descriptor
			else if (c == ';') {inType = false; continue;} // L defines the type, so this has already been counted
			else if (inType) continue;
			else if (c == 'L') inType = true;
			else if (c == '[') continue; // doesn't really mean anything to the count
			count++;
			//@formatter:on
		}
		
		return count;
	}
	
	public static int[] getIO(AbstractInsnNode insnNode) {
		if (insnNode instanceof FrameNode) return new int[]{0, 0};
		
		int sub = 0;
		int add = 0;
		if (
				insnNode instanceof LdcInsnNode ||
						insnNode instanceof LookupSwitchInsnNode ||
						insnNode instanceof TableSwitchInsnNode
		)
			add = 1;
		else if (insnNode instanceof IincInsnNode) {
			add = 1;
			sub = 1;
		} else if (insnNode instanceof TypeInsnNode) {
			if (insnNode.getOpcode() == Opcodes.NEW)
				return new int[]{1, 0};
			return new int[]{1, 1};
		} else if (
				insnNode instanceof LineNumberNode ||
						insnNode instanceof LabelNode
		) {
			return new int[]{sub, add};
		} else if (
				insnNode instanceof VarInsnNode ||
						insnNode instanceof IntInsnNode ||
						insnNode instanceof JumpInsnNode ||
						insnNode instanceof InsnNode
		) {
			Insn insn = Insn.getFromId(insnNode.getOpcode());
			if (insn == null) {
				if (true) {
					String name = getName(insnNode);
					System.err.println("Untracked opcode: " + name);
//					throw new RuntimeException("Insn " + insnNode.getOpcode() + " (" + insnNode.getClass() + ") not found");
				}
				add = 0;
				sub = 0;
			} else {
				add = insn.add;
				sub = insn.subtract;
			}
		} else if (insnNode instanceof FieldInsnNode) {
			if (insnNode.getOpcode() != Opcodes.GETSTATIC)
				sub = 1;
			add = 1;
		} else if (insnNode instanceof InvokeDynamicInsnNode) {
			InvokeDynamicInsnNode node = (InvokeDynamicInsnNode) insnNode;
			// code
			sub = descInput(node.desc);
			add = node.desc.endsWith("V") ? 0 : 1;
		} else if (insnNode instanceof MethodInsnNode) {
			MethodInsnNode node = (MethodInsnNode) insnNode;
			// code
			int count = descInput(node.desc);
			
			if (node.getOpcode() != Opcodes.INVOKESTATIC) count += 1;
			
			sub = count;
			add = node.desc.endsWith("V") ? 0 : 1;
		} else {
			if (insnNode == null) {
				throw new RuntimeException("null insn provided");
			}
			String name = getName(insnNode);
			throw new RuntimeException("NYI: " + insnNode.getClass() + " opcode: " + insnNode.getOpcode() + " name: " + name);
		}
		return new int[]{add, sub};
	}
	
	public static boolean isUnsatisfied(MethodInsnNode node) {
		int size = getIO(node)[1];
		
		AbstractInsnNode current = node.getPrevious();
		
		while (size > 0) {
			int[] io = getIO(current);
			size += io[0] - io[1];
			
			if (size > 0)
				current = current.getPrevious();
			
			if (current == null) break;
		}
		return size != 0;
	}
	
	protected static boolean forceCont(boolean exact, AbstractInsnNode node, int target, int size, int[] io) {
		if (exact)
			return size != target || io[1] != 0;
		else
			return io[1] != 0;
	}
	
	protected static void writeInto(AbstractInsnNode nd, StringBuilder builder, int size) {
		String str = BytecodeWriter.toString(null, nd, new HashMap<>());
		
		if (str != null) {
			if (str.contains("\n")) {
				str = str.substring(1).replace("\t\t", "");
				String[] split = str.split("\n");
				split[0] = split[0]
						.replace("-", "*")
						.replace("+", "-")
						.replace("*", "+");
				builder.append(split[0].replace("stack", "change")).append("\n");
				builder.append("# size: ").append(size).append("\n");
				builder.append(split[1]).append("\n");
			} else {
				builder.append(str.trim()).append("\n");
			}
		}
	}
	
	public static boolean doExact(MethodInsnNode insnNode) {
		return insnNode.getOpcode() != Opcodes.INVOKESPECIAL;
	}
	
	public static String getDebugString(MethodInsnNode call, AbstractInsnNode start) {
		StringBuilder path = new StringBuilder("\n");
		AbstractInsnNode print = call;
		int size = getIO(print)[1];
		
		writeInto(print, path, size);
		print = print.getPrevious();
		
		while (print != start) {
			int[] io = getIO(print);
			size += io[1] - io[0];
			writeInto(print, path, size);
			
			if (print instanceof MethodInsnNode) {
				MethodInsnNode methodNode = (MethodInsnNode) print;
				
				// code
				int[] remainder1 = new int[1];
				start = findStart(doExact(methodNode), methodNode, remainder1);
				size -= remainder1[0];
				
				String text = getDebugString(methodNode, start);
				String[] split = text.split("\n");
				if (split.length != 3) {
					for (int i = 0; i < split.length - 1; i++)
						path.append("\t| ").append(split[i]).append("\n");
				}
				
				if (methodNode.getOpcode() != Opcodes.INVOKESTATIC)
					size -= 1;
			}
			
			print = print.getPrevious();
		}
		
		int[] io = getIO(print);
		size += io[1] - io[0];
		writeInto(print, path, size);
		
		return path.toString().trim();
	}
	
	public static AbstractInsnNode findStart(boolean exact, MethodInsnNode node, int[] remainder) {
		int size = getIO(node)[1];
		int paramId = size;
		
		AbstractInsnNode current = node.getPrevious();
		boolean cont = false;
		boolean failed = false;
		try {
			while (size > 0 || cont) {
				if (current == null)
					break;
				
				if (current.getOpcode() == -1) {
					current = current.getPrevious();
					continue;
				}
				
				int[] io = getIO(current);
				// validate implemented
				if (io[0] == -1 || io[1] == -1)
					break;
				
				if (size == paramId) paramId--;
				if (size < paramId)
					// stack size dropped too low
					break;
				
				size += io[1] - io[0];
				
				boolean force = forceCont(exact, node, 0, size, io);
				if (size > 0 && !force) {
					current = current.getPrevious();
					cont = false;
				} else if (force) {
					current = current.getPrevious();
					cont = (size == 0);
				} else cont = false;
			}
		} catch (Throwable err) {
			System.err.println(err.getMessage());
			failed = true;
		}
		
		if ((exact ? (size != 0) : (size > 0)) || current == null || failed) {
			String path = getDebugString(node, current);
			System.err.println(path);
			throw new RuntimeException("Failed to track size of insn " + node.owner + "#" + node.name + node.desc);
		}
		
		remainder[0] = -size;
		return current;
	}
	
	public static AbstractInsnNode findEmpty(boolean exact, VarInsnNode node) {
		int size = getIO(node)[1];
		int paramId = size;
		
		AbstractInsnNode current = node.getPrevious();
		boolean cont = false;
		boolean failed = false;
		
		while (size > 0 || cont) {
			if (current.getOpcode() == -1) {
				current = current.getPrevious();
				continue;
			}
			
			int[] io = getIO(current);
			// validate implemented
			if (io[0] == -1 || io[1] == -1)
				break;
			
			size += io[1] - io[0];
			
			boolean force = forceCont(exact, node, 0, size, io);
			if (size > 0 && !force) {
				current = current.getPrevious();
				cont = false;
			} else if (force) {
				current = current.getPrevious();
				cont = (size == 0);
			} else cont = false;
			
			if (current == null)
				break;
		}
		
		return current;
	}
}
