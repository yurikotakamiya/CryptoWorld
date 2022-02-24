package cw.common.event;

public interface IEventHandler {
    void process(IEvent event) throws Exception;
}
