# What happened to the old samples?

The old samples have been removed from the project because of their CC-BY-NC-ND license, which we cannot change.

New samples are going to be created from scratch. Please be patient.

## Where can I find the original samples?

The original samples can still be found in the [sustaining branch](https://github.com/WrenSecurity/wrenidm/tree/sustaining/5.x/openidm-zip/src/main/resources/samples).

You might get `Unknown property used in expression: ${decision=='accept'}` error when running the old workflow based samples. This error is caused by a backward-incompatible change in Flowable Framework integration, where the variables created in tasks are no longer stored in the process scope by default.

The problem can be fixed using the following code:

```
<userTask ...>
    ...
    <extensionElements>
         ...
         <!-- Paste taskListener inside extensionElements within task -->
        <flowable:taskListener event="complete" class="org.flowable.engine.impl.bpmn.listener.ScriptTaskListener">
            <flowable:field name="script">
                <flowable:string>
                    task.getExecution().setVariable("decision", decision)
                </flowable:string>
            </flowable:field>
            <flowable:field name="language" stringValue="groovy"/>
        </flowable:taskListener>
        ...
    </extensionElements>
    ...
</userTask>
````


