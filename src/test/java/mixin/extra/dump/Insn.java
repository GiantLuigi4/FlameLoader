package mixin.extra.dump;

import org.objectweb.asm.tree.AbstractInsnNode;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public enum Insn {
	// int math
	IDIV(108, 1, 2),
	IMUL(104, 1, 2),
	IADD(96, 1, 2),
	ISUB(100, 1, 2),
	IREM(112, 1, 2),
	ISHL(120, 1, 2),
	ISHR(122, 1, 2),
	// int logic
	IXOR(130, 1, 2),
	IOR(128, 1, 2),
	IUSHR(124, 1, 2),
	IAND(126, 1, 2),
	INEG(116, 1, 2),
	// long math
	LDIV(109, 1, 2),
	LMUL(105, 1, 2),
	LADD(97, 1, 2),
	LSUB(101, 1, 2),
	LREM(113, 1, 2),
	LSHL(121, 1, 2),
	LSHR(123, 1, 2),
	// long logic
	LXOR(131, 1, 2),
	LOR(129, 1, 2),
	LUSHR(125, 1, 2),
	LAND(127, 1, 2),
	LCMP(148, 1, 2),
	LNEG(117, 1, 2),
	// float math
	FDIV(110, 1, 2),
	FMUL(106, 1, 2),
	FADD(98, 1, 2),
	FSUB(102, 1, 2),
	FREM(114, 1, 2),
	// float logic
	FCMPG(150, 1, 2),
	FCMPL(149, 1, 2),
	FNEG(118, 1, 2),
	// double math
	DDIV(111, 1, 2),
	DMUL(107, 1, 2),
	DADD(99, 1, 2),
	DSUB(103, 1, 2),
	DREM(115, 1, 2),
	// double logic
	DCMPG(152, 1, 2),
	DCMPL(151, 1, 2),
	DNEG(119, 1, 2),
	// objects
	NEW(187, 1, 0),
	MONITORENTER(194, 1, 0),
	MONITOREXIT(195, 1, 0),
	ATHROW(191, 1, 1),
	RETURN(177, 0, 0),
	ARETURN(176, 0, 1),
	DRETURN(175, 0, 1),
	FRETURN(174, 0, 1),
	IRETURN(172, 0, 1),
	LRETURN(173, 0, 1),
	// stack manipulation
	DUP(89, 2, 1),
	POP(87, 0, 1),
	SWAP(95, 2, 2),
	BIPUSH(16, 1, 0),
	SIPUSH(17, 1, 0),
	// jumps
	IFEQ(153, 0, 1),
	IFNE(154, 0, 1),
	IFGE(156, 0, 1),
	IFGT(157, 0, 1),
	IFLE(158, 0, 1),
	IFLT(155, 0, 1),
	IFNONNULL(199, 0, 1),
	IFNULL(198, 0, 1),
	IF_ICMPEQ(159, 0, 2),
	IF_ICMPNE(160, 0, 2),
	IF_ICMPGE(162, 0, 2),
	IF_ICMPGT(163, 0, 2),
	IF_ICMPLE(164, 0, 2),
	IF_ICMPLT(161, 0, 2),
	IF_ACMPEQ(165, 0, 2),
	IF_ACMPNE(166, 0, 2),
	GOTO(167, 0, 0),
	// switches
	TABLESWITCH(170, 0, 1),
	LOOKUPSWITCH(171, 0, 1),
	// load local
	ALOAD(25, 1, 0),
	DLOAD(24, 1, 0),
	FLOAD(23, 1, 0),
	ILOAD(21, 1, 0),
	LLOAD(22, 1, 0),
	// set local
	ASTORE(58, 0, 1),
	DSTORE(57, 0, 1),
	FSTORE(56, 0, 1),
	ISTORE(54, 0, 1),
	LSTORE(55, 0, 1),
	// array manipulation
	ANEWARRAY(189, 1, 1),
	NEWARRAY(188, 1, 1),
	ARRAYLENGTH(190, 1, 1),
	// set array element
	AASTORE(83, 0, 3),
	BASTORE(84, 0, 3),
	CASTORE(85, 0, 3),
	DASTORE(82, 0, 3),
	FASTORE(81, 0, 3),
	IASTORE(79, 0, 3),
	LASTORE(80, 0, 3),
	SASTORE(86, 0, 3),
	// load array element
	AALOAD(50, 1, 2),
	BALOAD(51, 1, 2),
	CALOAD(52, 1, 2),
	DALOAD(49, 1, 2),
	FALOAD(48, 1, 2),
	IALOAD(46, 1, 2),
	LALOAD(47, 1, 2),
	SALOAD(53, 1, 2),
	// constants
	ACONST_NULL(1, 1, 0),
	DCONST_0(14, 1, 0),
	DCONST_1(15, 1, 0),
	FCONST_0(11, 1, 0),
	FCONST_1(12, 1, 0),
	FCONST_2(13, 1, 0),
	ICONST_0(3, 1, 0),
	ICONST_1(4, 1, 0),
	ICONST_M1(2, 1, 0),
	ICONST_2(5, 1, 0),
	ICONST_3(6, 1, 0),
	ICONST_4(7, 1, 0),
	ICONST_5(8, 1, 0),
	LCONST_0(9, 1, 0),
	LCONST_1(10, 1, 0),
	// casts
	D2F(144, 1, 1),
	D2I(142, 1, 1),
	D2L(143, 1, 1),
	F2D(141, 1, 1),
	F2I(139, 1, 1),
	F2L(140, 1, 1),
	I2B(145, 1, 1),
	I2C(146, 1, 1),
	I2D(135, 1, 1),
	I2F(134, 1, 1),
	I2L(133, 1, 1),
	I2S(147, 1, 1),
	L2D(138, 1, 1),
	L2F(137, 1, 1),
	L2I(136, 1, 1),
	;
	
	public final int
			id, // the id of the insn
			add, // amount of entries added to the stack
			subtract // amount of entries removed from the stack
					;
	
	Insn(int id, int add, int subtract) {
		this.id = id;
		this.add = add;
		this.subtract = subtract;
	}
	
	public static Insn getFromId(int id) {
		for (Insn value : values())
			if (value.id == id)
				return value;
		return null;
	}
	
	public static boolean isLoad(AbstractInsnNode current) {
		Insn insn = getFromId(current.getOpcode());
		if (insn == null) return false;
		return insn.name().endsWith("LOAD");
	}
}
