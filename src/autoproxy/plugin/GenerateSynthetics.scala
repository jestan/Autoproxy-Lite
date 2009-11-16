package autoproxy.plugin

import scala.tools._
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent
import nsc.transform.Transform
import nsc.transform.InfoTransform
import nsc.transform.TypingTransformers
import nsc.symtab.Flags._
import nsc.util.Position
import nsc.ast.TreeDSL
import nsc.typechecker
import scala.annotation.tailrec


class GenerateSynthetics(plugin: AutoProxyPlugin, val global : Global) extends PluginComponent
  with Transform
  with TypingTransformers
  with TreeDSL
{
  import global._
  import definitions._
  	  
  val runsAfter = List[String]("earlytyper")
  val phaseName = "generatesynthetics"
  def newTransformer(unit: CompilationUnit) = new AutoProxyTransformer(unit)    

  class AutoProxyTransformer(unit: CompilationUnit) extends TypingTransformer(unit) {
    import CODE._

    private def mkDelegate(owner: Symbol, tgtMember: Symbol, tgtMethod: Symbol, pos: Position) = {
      val delegate = cloneMethod(tgtMethod, owner)
	        
      log("owner=" + This(owner))
	  
      val selectTarget = This(owner) DOT tgtMember DOT tgtMethod
      log("SelectTarget=")
      log(nodeToString(selectTarget))
	  
      val rhs : Tree =
        delegate.info match {
          case MethodType(params, _) => Apply(selectTarget, params.map(Ident(_)))
          case _ => selectTarget
        }
	    
      val delegateDef = localTyper.typed { DEF(delegate) === rhs } 
	    
      log(nodePrinters nodeToString delegateDef)
    
      delegateDef
    }
	
    private def publicMembersOf(sym:Symbol) =
      sym.tpe.members.filter(_.isPublic).filter(!_.isConstructor)
	
    private def publicMethodsOf(sym:Symbol) =
      publicMembersOf(sym).filter(_.isMethod)
  
    private def cloneMethod(prototype: Symbol, owner: Symbol) = {
      val newSym = prototype.cloneSymbol(owner)
      newSym setPos owner.pos.focus
      newSym setFlag SYNTHETICMETH
      owner.info.decls enter newSym    	
    }
      
    
    def generateDelegates(templ: Template, memberToProxy: Symbol) : List[Tree] = {
      val cls = memberToProxy.owner  //the class owning the symbol
    	
      log("found annotated member: " + memberToProxy)
      log("owning class: " + cls)
        
      val definedMethods = publicMembersOf(cls)
      val requiredMethods =
        publicMembersOf(memberToProxy).filter(mem => !definedMethods.contains(mem))
    	
      log("defined methods: " + definedMethods.mkString(", "))
      log("missing methods: " + requiredMethods.mkString(", "))

      val synthetics = for (method <- requiredMethods) yield
        mkDelegate(cls, memberToProxy, method, memberToProxy.pos.focus)
      
      synthetics
    }
   
    override def transform(tree: Tree) : Tree = {
	  def shouldAutoProxy(tree: Tree) = 
	      tree.symbol.annotations exists { _.toString == plugin.AutoproxyAnnotationClass }
		   
	  val newTree = tree match {
	    case ClassDef(mods,name,tparams,impl) =>
	      val delegs = for (member <- impl.body if shouldAutoProxy(member)) yield
	        generateDelegates(impl, member.symbol)
	      val newImpl = treeCopy.Template(impl, impl.parents, impl.self, delegs.flatten ::: impl.body)
	      treeCopy.ClassDef(tree, mods, name, tparams, newImpl)
	    case _ => tree
	  }
	  super.transform(newTree)
	}    
  }
}