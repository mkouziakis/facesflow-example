/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.oio.jsfexamples.facesflows.flowa;

import java.io.Serializable;
import java.util.logging.Logger;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.enterprise.inject.Produces;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.flow.Flow;
import javax.faces.flow.builder.FlowBuilder;
import javax.faces.flow.builder.FlowBuilderParameter;
import javax.faces.flow.builder.FlowDefinition;

/**
 *
 * @author Thomas Asel
 */
public class FlowFactory implements Serializable {
    
    private final static Logger LOG = Logger.getLogger(FlowFactory.class.getSimpleName());
    private static final long serialVersionUID = 1L;

    @Produces @FlowDefinition
    public Flow defineFlow( @FlowBuilderParameter FlowBuilder flowBuilder) {
        String flowId = "flow-a";   // id for this flow 
        flowBuilder.id("", flowId); // set flow id
        
        // add a view to the flow and mark it as start node of the flow graph
        flowBuilder.viewNode(flowId, "/" + flowId + "/" + flowId + ".xhtml").markAsStartNode(); 
        
        // add another view to the flow 
        flowBuilder.viewNode("flow-a-page-2", "/" + flowId + "/page2.xhtml");   

        // add another view to the flow 
        flowBuilder.viewNode("flow-a-page-3", "/common-page.xhtml"); 
        
        FacesContext ctx = FacesContext.getCurrentInstance();
        ELContext elContext = ctx.getELContext();
        Application application = ctx.getApplication();
        ExpressionFactory expFactory = application.getExpressionFactory();
        
        // add a method call node to the flow that transitions to another node
        flowBuilder.methodCallNode("method-node")       // name of the node
                .defaultOutcome("flow-a-page-3")        // use this outcome, if the method doens not return a different one
                .expression("#{flowABean.methodFlowNode('test')}", new Class[]{String.class});  // expression to invoke
   
        // add a method call node to the flow that exits the flow by returning an outcome containing the node id of the return node
        flowBuilder.methodCallNode("exit-method-node")
                .expression("#{flowABean.exitMethodFlowNode()}");
        
        flowBuilder.switchNode("switch-node")   // create a switch node
                .defaultOutcome("home")         // exit flow to homepage, if no valid selection happened
                // several switch statements
                .switchCase()  
                    .condition("#{flowABean.selectedPage==1}").fromOutcome("flow-a")
                .switchCase()
                    .condition("#{flowABean.selectedPage==2}").fromOutcome("flow-a-page-2")
                .switchCase()
                    .condition("#{flowABean.selectedPage==3}").fromOutcome("flow-a-page-3")
                .switchCase()
                    .condition("#{flowABean.selectedPage==4}").fromOutcome("return-node");

        // call this when the flow is entered
        flowBuilder.initializer("#{flowABean.initialize()}");
        
        // call this when the flow is exited
        flowBuilder.finalizer("#{flowABean.finalize()}");
        
        // add a return node. The flow is exited with the outcome "home" once this node is reached.
        flowBuilder.returnNode("return-node").fromOutcome("home");
        
        return flowBuilder.getFlow();
    }
}