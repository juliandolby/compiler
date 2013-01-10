package sizzle.functions;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sizzle.types.Ast.*;
import sizzle.types.Ast.Modifier.ModifierKind;

/**
 * Boa domain-specific functions for finding Java language features.
 * 
 * @author rdyer
 */
public class BoaJavaFeaturesIntrinsics {
	////////////////////////
	// Enhanced For Loops //
	////////////////////////

	private static class EnhancedForVisitor extends BoaCountingVisitor {
		@Override
		protected boolean preVisit(Statement node) {
			if (node.getKind() == Statement.StatementKind.FOR && node.hasVariableDeclaration())
				count++;
			return true;
		}
	}
	private static EnhancedForVisitor enhancedForVisitor = new EnhancedForVisitor();

	@FunctionSpec(name = "uses_enhanced_for", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesEnhancedFor(final ASTRoot f) {
		enhancedForVisitor.initialize().visit(f);
		return enhancedForVisitor.count;
	}

	/////////////
	// Varargs //
	/////////////

	private static class VarargsVisitor extends BoaCountingVisitor {
		@Override
		protected boolean preVisit(Method node) {
			if (node.getArgumentsCount() > 0)
				if (node.getArguments(node.getArgumentsCount() - 1).getVariableType().getName().contains("..."))
					count++;
			return true;
		}
	}
	private static VarargsVisitor varargsVisitor = new VarargsVisitor();

	@FunctionSpec(name = "uses_varargs", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesVarargs(final ASTRoot f) {
		varargsVisitor.initialize().visit(f);
		return varargsVisitor.count;
	}

	///////////////////////
	// Assert Statements //
	///////////////////////

	private static class AssertVisitor extends BoaCountingVisitor {
		@Override
		protected boolean preVisit(Statement node) {
			if (node.getKind() == Statement.StatementKind.ASSERT)
				count++;
			return true;
		}
	}
	private static AssertVisitor assertVisitor = new AssertVisitor();

	@FunctionSpec(name = "uses_assert", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesAssert(final ASTRoot f) {
		assertVisitor.initialize().visit(f);
		return assertVisitor.count;
	}

	///////////////////////
	// Enum Declarations //
	///////////////////////

	private static class EnumVisitor extends BoaCountingVisitor {
		@Override
		protected boolean preVisit(Declaration node) {
			if (node.getKind() == TypeKind.ENUM)
				count++;
			return true;
		}
	}
	private static EnumVisitor enumVisitor = new EnumVisitor();

	@FunctionSpec(name = "uses_enums", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesEnums(final ASTRoot f) {
		enumVisitor.initialize().visit(f);
		return enumVisitor.count;
	}

	//////////////////////////////////
	// Try with Resources Statement //
	//////////////////////////////////

	private static class TryResourcesVisitor extends BoaCountingVisitor {
		@Override
		protected boolean preVisit(Statement node) {
			if (node.getKind() == Statement.StatementKind.TRY && node.getInitializationsCount() > 0)
				count++;
			return true;
		}
	}
	private static TryResourcesVisitor tryResourcesVisitor = new TryResourcesVisitor();

	@FunctionSpec(name = "uses_try_resources", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesTryResources(final ASTRoot f) {
		tryResourcesVisitor.initialize().visit(f);
		return tryResourcesVisitor.count;
	}

	/////////////////////////////////
	// Generics - Type Declaration //
	/////////////////////////////////

	private static class GenericTypeVisitor extends BoaCountingVisitor {
		@Override
		protected boolean preVisit(Declaration node) {
			if (node.getGenericParametersCount() > 0)
				count++;
			return true;
		}
	}
	private static GenericTypeVisitor genericTypeVisitor = new GenericTypeVisitor();

	@FunctionSpec(name = "uses_generics_define_type", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesGenericsDefineType(final ASTRoot f) {
		genericTypeVisitor.initialize().visit(f);
		return genericTypeVisitor.count;
	}

	///////////////////////////////////
	// Generics - Method Declaration //
	///////////////////////////////////

	private static class GenericMethodVisitor extends BoaCountingVisitor {
		@Override
		protected boolean preVisit(Method node) {
			if (node.getGenericParametersCount() > 0)
				count++;
			return true;
		}
	}
	private static GenericMethodVisitor genericMethodVisitor = new GenericMethodVisitor();

	@FunctionSpec(name = "uses_generics_define_method", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesGenericsDefineMethod(final ASTRoot f) {
		genericMethodVisitor.initialize().visit(f);
		return genericMethodVisitor.count;
	}

	//////////////////////////////////
	// Generics - Field Declaration //
	//////////////////////////////////

	private static class GenericFieldVisitor extends BoaCountingVisitor {
		@Override
		protected boolean preVisit(Type node) {
			if (node.getName().contains("<"))
				count++;
			return true;
		}
		@Override
		protected boolean preVisit(Declaration node) {
			final List<Method> methodsList = node.getMethodsList();
			for (int i = 0; i < methodsList.size(); i++)
				visit(methodsList.get(i));

			final List<Variable> fieldsList = node.getFieldsList();
			for (int i = 0; i < fieldsList.size(); i++)
				visit(fieldsList.get(i));

			final List<Declaration> nestedList = node.getNestedDeclarationsList();
			for (int i = 0; i < nestedList.size(); i++)
				visit(nestedList.get(i));
	
			return false;
		}
		@Override
		protected boolean preVisit(Method node) {
			final List<Statement> statementsList = node.getStatementsList();
			for (int i = 0; i < statementsList.size(); i++)
				visit(statementsList.get(i));

			return false;
		}
		@Override
		protected boolean preVisit(Statement node) {
			final List<Statement> statementsList = node.getStatementsList();
			for (int i = 0; i < statementsList.size(); i++)
				visit(statementsList.get(i));

			if (node.hasTypeDeclaration())
				visit(node.getTypeDeclaration());

			return false;
		}
		@Override
		protected boolean preVisit(Expression node) {
			return false;
		}
		@Override
		protected boolean preVisit(Modifier node) {
			return false;
		}
	}
	private static GenericFieldVisitor genericFieldVisitor = new GenericFieldVisitor();

	@FunctionSpec(name = "uses_generics_define_field", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesGenericsDefineField(final ASTRoot f) {
		genericFieldVisitor.initialize().visit(f);
		return genericFieldVisitor.count;
	}

	///////////////////////////////
	// Generics - Super Wildcard //
	///////////////////////////////

	private static Matcher wildcardSuperMatcher = Pattern.compile("\\?\\s*super\\s+.+").matcher("");

	private static class GenericSuperWildcardVisitor extends BoaCountingVisitor {
		@Override
		protected boolean preVisit(Type node) {
			if (wildcardSuperMatcher.reset(node.getName()).find())
				count++;
			return true;
		}
	}
	private static GenericSuperWildcardVisitor genericSuperWildcardVisitor = new GenericSuperWildcardVisitor();

	@FunctionSpec(name = "uses_generics_wildcard_super", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesGenericsWildcardSuper(final ASTRoot f) {
		genericSuperWildcardVisitor.initialize().visit(f);
		return genericSuperWildcardVisitor.count;
	}

	/////////////////////////////////
	// Generics - Extends Wildcard //
	/////////////////////////////////

	private static Matcher wildcardExtendsMatcher = Pattern.compile("\\?\\s*extends\\s+.+").matcher("");

	private static class GenericExtendsWildcardVisitor extends BoaCountingVisitor {
		@Override
		protected boolean preVisit(Type node) {
			if (wildcardExtendsMatcher.reset(node.getName()).find())
				count++;
			return true;
		}
	}
	private static GenericExtendsWildcardVisitor genericExtendsWildcardVisitor = new GenericExtendsWildcardVisitor();

	@FunctionSpec(name = "uses_generics_wildcard_extends", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesGenericsWildcardExtends(final ASTRoot f) {
		genericExtendsWildcardVisitor.initialize().visit(f);
		return genericExtendsWildcardVisitor.count;
	}

	////////////////////////////////
	// Generics - Other Wildcards //
	////////////////////////////////

	private static class GenericOtherWildcardVisitor extends BoaCountingVisitor {
		@Override
		protected boolean preVisit(Type node) {
			if (node.getName().contains("?")
					&& !wildcardExtendsMatcher.reset(node.getName()).find()
					&& !wildcardSuperMatcher.reset(node.getName()).find())
				count++;
			return true;
		}
	}
	private static GenericOtherWildcardVisitor genericOtherWildcardVisitor = new GenericOtherWildcardVisitor();

	@FunctionSpec(name = "uses_generics_wildcard", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesGenericsWildcard(final ASTRoot f) {
		genericOtherWildcardVisitor.initialize().visit(f);
		return genericOtherWildcardVisitor.count;
	}

	////////////////////////////////////
	// Generics - Declares Annotation //
	////////////////////////////////////

	private static class DeclaresAnnotationVisitor extends BoaCountingVisitor {
		@Override
		protected boolean preVisit(Declaration node) {
			if (node.getKind() == TypeKind.ANNOTATION)
				count++;
			return true;
		}
	}
	private static DeclaresAnnotationVisitor declaresAnnotationVisitor = new DeclaresAnnotationVisitor();

	@FunctionSpec(name = "uses_annotations_define", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesAnnotationsDefine(final ASTRoot f) {
		declaresAnnotationVisitor.initialize().visit(f);
		return declaresAnnotationVisitor.count;
	}

	////////////////////////////////
	// Generics - Uses Annotation //
	////////////////////////////////

	private static class UsesAnnotationVisitor extends BoaCountingVisitor {
		@Override
		protected boolean preVisit(Modifier node) {
			if (node.getKind() == ModifierKind.ANNOTATION)
				count++;
			return true;
		}
	}
	private static UsesAnnotationVisitor usesAnnotationVisitor = new UsesAnnotationVisitor();

	@FunctionSpec(name = "uses_annotations_uses", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesAnnotationsUses(final ASTRoot f) {
		usesAnnotationVisitor.initialize().visit(f);
		return usesAnnotationVisitor.count;
	}

	/////////////////////////////
	// Mutiple-Exception Catch //
	/////////////////////////////

	private static class MultiCatchVisitor extends BoaCountingVisitor {
		@Override
		protected boolean preVisit(Statement node) {
			if (node.getKind() == Statement.StatementKind.CATCH && node.getVariableDeclaration().getVariableType().getName().contains("|"))
				count++;
			return true;
		}
	}
	private static MultiCatchVisitor multiCatchVisitor = new MultiCatchVisitor();

	@FunctionSpec(name = "uses_multi_catch", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesMultiCatch(final ASTRoot f) {
		multiCatchVisitor.initialize().visit(f);
		return multiCatchVisitor.count;
	}

	////////////////////
	// Binary Literal //
	////////////////////

	// this regex is a bit generous, but will definitely find what we want
	// the invalid things it allows are actually invalid syntax, so who
	// cares (we only deal with error-free parsed source)
	private static Matcher binaryMatcher = Pattern.compile("0[bB][01][01_]*[01][L]?").matcher("");

	private static class BinaryLitVisitor extends BoaCountingVisitor {
		@Override
		protected boolean preVisit(Expression node) {
			if (node.getKind() == Expression.ExpressionKind.LITERAL && node.hasLiteral() && binaryMatcher.reset(node.getLiteral()).matches())
				count++;
			return true;
		}
	}
	private static BinaryLitVisitor binaryLitVisitor = new BinaryLitVisitor();

	@FunctionSpec(name = "uses_binary_lit", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesBinaryLit(final ASTRoot f) {
		binaryLitVisitor.initialize().visit(f);
		return binaryLitVisitor.count;
	}

	////////////////////////
	// Underscore Literal //
	////////////////////////

	// this regex is a bit generous, but will definitely find what we want
	// the invalid things it allows are actually invalid syntax, so who
	// cares (we only deal with error-free parsed source)
	private static Matcher underscoreMatcher = Pattern.compile("(0[bBx])?([0-9]+.[0-9]+)?[0-9A-Fa-f]([0-9A-Fa-f_])*[0-9A-Fa-f][FL]?").matcher("");

	private static class UnderscoreLitVisitor extends BoaCountingVisitor {
		@Override
		protected boolean preVisit(Expression node) {
			if (node.getKind() == Expression.ExpressionKind.LITERAL && node.hasLiteral()
					&& node.getLiteral().contains("_") && underscoreMatcher.reset(node.getLiteral()).matches())
				count++;
			return true;
		}
	}
	private static UnderscoreLitVisitor underscoreLitVisitor = new UnderscoreLitVisitor();

	@FunctionSpec(name = "uses_underscore_lit", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesUnderscoreLit(final ASTRoot f) {
		underscoreLitVisitor.initialize().visit(f);
		return underscoreLitVisitor.count;
	}

	//////////////////////
	// Diamond Operator //
	//////////////////////

	private static class DiamondVisitor extends BoaCountingVisitor {
		@Override
		protected boolean preVisit(Type node) {
			if (node.getName().contains("<>"))
				count++;
			return true;
		}
	}
	private static DiamondVisitor diamondVisitor = new DiamondVisitor();

	@FunctionSpec(name = "uses_diamond", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesDiamond(final ASTRoot f) {
		diamondVisitor.initialize().visit(f);
		return diamondVisitor.count;
	}

	//////////////////
	// Safe Varargs //
	//////////////////

	private static class SafeVarargsVisitor extends BoaCountingVisitor {
		@Override
		protected boolean preVisit(Method node) {
			// @SafeVarargs
			if (BoaModifierIntrinsics.hasAnnotation(node, "SafeVarargs"))
				count++;

			// @SuppressWarnings({"unchecked", "varargs"})
			Modifier mod = BoaModifierIntrinsics.getAnnotation(node, "SuppressWarnings");
			if (mod != null)
				for (int i = 0; i < mod.getAnnotationMembersCount(); i++)
					if (mod.getAnnotationMembers(i).equals("value")) {
						Expression e = mod.getAnnotationValues(i);
						if (e.getKind() == Expression.ExpressionKind.ARRAYINIT) {
							boolean foundUnchecked = false, foundVarargs = false;
							for (int j = 0; j < e.getExpressionsCount(); j++)
								if (e.getExpressions(j).getKind() == Expression.ExpressionKind.LITERAL) {
									if (e.getExpressions(j).getLiteral().equals("unchecked"))
										foundUnchecked = true;
									if (e.getExpressions(j).getLiteral().equals("varargs"))
										foundVarargs = true;
								}
							// TODO verify this works
							if (foundUnchecked && foundVarargs)
								count++;
						}
						break;
					}

			return true;
		}
	}
	private static SafeVarargsVisitor safeVarargsVisitor = new SafeVarargsVisitor();

	@FunctionSpec(name = "uses_safe_varargs", returnType = "int", formalParameters = { "ASTRoot" })
	public static long usesSafeVarargs(final ASTRoot f) {
		safeVarargsVisitor.initialize().visit(f);
		return safeVarargsVisitor.count;
	}

	//////////////////////////////
	// Collect Annotations Used //
	//////////////////////////////

	private static class AnnotationCollectingVisitor extends BoaAbstractVisitor {
		public HashMap<String,Long> counts;
		public AnnotationCollectingVisitor initialize(final HashMap<String,Long> counts) {
			this.counts = counts;
			return this;
		}

		@Override
		protected boolean preVisit(Modifier node) {
			if (node.getKind() == Modifier.ModifierKind.ANNOTATION) {
				final String name = BoaAstIntrinsics.type_name(node.getAnnotationName());
				final long count = counts.containsKey(name) ? counts.get(name) : 0;
				counts.put(name, count + 1);
			}
			return true;
		}
	}
	private static AnnotationCollectingVisitor annotationCollectingVisitor = new AnnotationCollectingVisitor();

	@FunctionSpec(name = "collect_annotations", returnType = "map[string] of int", formalParameters = { "ASTRoot", "map[string] of int" })
	public static HashMap<String,Long> collect_annotations(final ASTRoot f, HashMap<String,Long> counts) {
		annotationCollectingVisitor.initialize(counts).visit(f);
		return annotationCollectingVisitor.counts;
	}

	///////////////////////////
	// Collect Generics Used //
	///////////////////////////

	private static class GenericsCollectingVisitor extends BoaAbstractVisitor {
		public HashMap<String,Long> counts;
		public GenericsCollectingVisitor initialize(final HashMap<String,Long> counts) {
			this.counts = counts;
			return this;
		}
		@Override
		protected boolean preVisit(Type node) {
			try {
				parseGenericType(BoaAstIntrinsics.type_name(node.getName()).trim(), counts);
			} catch (final StackOverflowError e) {
				System.err.println("STACK ERR: " + node.getName() + " -> " + BoaAstIntrinsics.type_name(node.getName()).trim());
			}
			return true;
		}
	}
	private static GenericsCollectingVisitor genericsCollectingVisitor = new GenericsCollectingVisitor();

	@FunctionSpec(name = "collect_generic_types", returnType = "map[string] of int", formalParameters = { "ASTRoot", "map[string] of int" })
	public static HashMap<String,Long> collect_generic_types(final ASTRoot f, HashMap<String,Long> counts) {
		genericsCollectingVisitor.initialize(counts).visit(f);
		return genericsCollectingVisitor.counts;
	}

	public static void testGenericParser() {
		final String[] tests = new String[] {
			"$_T4_$_ extends $Mc4$<Integer>.$Mc5<Integer>",
			"$_T4_$_ extends $Mc4$<Integer>.$Mc5<Integer> & $Mi4$.$$$$$Mi5 & $Mi4$.$$$$Mi5 & $Mi4$.$$$Mi5 & $Mi4$.$$Mi5 & $Mi4$.$Mi5 & $Mi4$.Mi5 & $Mi4$",
			"ClassException<E> | Exception",
			"EextendsEnum<E>&Language<E>",
			"Class<?>...",
			"List<String>",
			"HashMap<String,Integer>",
			"HashMap<String, Integer>",
			"HashMap<String, List<Integer>>",
			"HashMap<String, HashMap<String, List<Integer>>>"
		};
		for (final String s : tests)
			testGeneric(s);
	}

	public static void testGeneric(final String s) {
		System.out.println("-----------------");
		System.out.println("testing: " + s);
		final HashMap<String,Long> counts = new HashMap<String,Long>();
		parseGenericType(s, counts);
		System.out.println(counts);
	}

	private static void parseGenericType(final String name, final HashMap<String,Long> counts) {
		if (!name.contains("<") || name.startsWith("<"))
			return;

		if (name.contains("|")) {
			for (final String s : name.split("\\|"))
				parseGenericType(s.trim(), counts);
			return;
		}

		if (name.contains("&")) {
			int count = 0;
			int last = 0;
			for (int i = 0; i < name.length(); i++)
				switch (name.charAt(i)) {
				case '<':
					count++;
					break;
				case '>':
					count--;
					break;
				case '&':
					if (count == 0) {
						parseGenericType(name.substring(last, i).trim(), counts);
						last = i + 1;
					}
					break;
				default:
					break;
				}
			parseGenericType(name.substring(last).trim(), counts);
			return;
		}

		foundType(name, counts);

		int start = name.indexOf("<");

		final Stack<Integer> starts = new Stack<Integer>();
		int lastStart = start + 1;
		for (int i = lastStart; i < name.lastIndexOf(">"); i++)
			switch (name.charAt(i)) {
			case '<':
				starts.push(lastStart);
				lastStart = i + 1;
				break;
			case '>':
				if (!starts.empty())
					foundType(name.substring(starts.pop(), i + 1).trim(), counts);
				break;
			case '&':
			case '|':
			case ',':
			case ' ':
			case '.':
			case '\t':
				lastStart = i + 1;
			default:
				break;
			}
	}

	private static void foundType(final String name, final HashMap<String,Long> counts) {
		final String type = name.endsWith("...") ? name.substring(0, name.length() - 3).trim() : name.trim();
		final long count = counts.containsKey(type) ? counts.get(type) : 0;
		counts.put(type, count + 1);

		String rawType = type.substring(0, type.indexOf("<")).trim();
		if (!type.endsWith(">"))
			rawType += type.substring(type.lastIndexOf(">") + 1).trim();
		final long rawCount = counts.containsKey(rawType) ? counts.get(rawType) : 0;
		counts.put(rawType, rawCount + 1);
	}
}
