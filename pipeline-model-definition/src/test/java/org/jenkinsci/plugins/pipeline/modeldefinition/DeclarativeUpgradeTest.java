/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.pipeline.modeldefinition;

import hudson.model.ParametersDefinitionProperty;
import jenkins.model.BuildDiscarderProperty;
import org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobPropertyTrackerAction;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.job.properties.DisableConcurrentBuildsJobProperty;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DeclarativeUpgradeTest extends AbstractDeclarativeTest {
    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @LocalData
    @Test
    public void trackerPropertyUpgrade() throws Exception {
        WorkflowJob p = j.jenkins.getItemByFullName("trackerPropertyUpgrade", WorkflowJob.class);
        assertNotNull(p);
        WorkflowRun b1 = p.getLastBuild();
        assertNotNull(b1);
        assertNotNull(b1.getAction(DeclarativeJobPropertyTrackerAction.class));
        assertNull(p.getAction(DeclarativeJobPropertyTrackerAction.class));

        p.addProperty(new DisableConcurrentBuildsJobProperty());

        p.setDefinition(new CpsFlowDefinition(pipelineSourceFromResources("simpleParameters"), true));
        WorkflowRun b2 = j.buildAndAssertSuccess(p);

        assertNull(b2.getAction(DeclarativeJobPropertyTrackerAction.class));
        assertNull(p.getProperty(BuildDiscarderProperty.class));
        ParametersDefinitionProperty parameters = p.getProperty(ParametersDefinitionProperty.class);
        assertNotNull(parameters);
        assertNotNull(parameters.getParameterDefinition("flag"));

        DeclarativeJobPropertyTrackerAction action2 = p.getAction(DeclarativeJobPropertyTrackerAction.class);
        assertNotNull(action2);
        assertFalse(action2.getParameters().isEmpty());
        assertEquals("flag", action2.getParameters().iterator().next());

        p.setDefinition(new CpsFlowDefinition(pipelineSourceFromResources("propsTriggersParamsRemoved"), true));
        WorkflowRun b3 = j.buildAndAssertSuccess(p);

        assertNull(b3.getAction(DeclarativeJobPropertyTrackerAction.class));
        assertNull(p.getProperty(BuildDiscarderProperty.class));
        assertNull(p.getProperty(ParametersDefinitionProperty.class));
        DeclarativeJobPropertyTrackerAction action3 = p.getAction(DeclarativeJobPropertyTrackerAction.class);
        assertNotNull(action3);
        assertTrue(action3.getParameters().isEmpty());
    }
}
