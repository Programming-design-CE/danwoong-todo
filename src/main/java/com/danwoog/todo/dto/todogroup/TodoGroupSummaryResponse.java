package com.danwoog.todo.dto.todogroup;

import com.danwoog.todo.domain.todogroup.GroupStatus;
import com.danwoog.todo.domain.todogroup.Priority;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class TodoGroupSummaryResponse {

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("group_name")
    private String groupName;

    @JsonProperty("deadline")
    private LocalDateTime deadline;

    @JsonProperty("priority")
    private Priority priority;

    @JsonProperty("status")
    private GroupStatus status;

    @JsonProperty("member_preview")
    private List<MemberPreviewResponse> memberPreview;

    @JsonProperty("member_count")
    private int memberCount;

    public TodoGroupSummaryResponse(Long groupId, String groupName, LocalDateTime deadline,
                                    Priority priority, GroupStatus status,
                                    List<MemberPreviewResponse> memberPreview, int memberCount) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.deadline = deadline;
        this.priority = priority;
        this.status = status;
        this.memberPreview = memberPreview;
        this.memberCount = memberCount;
    }

    public Long getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public LocalDateTime getDeadline() { return deadline; }
    public Priority getPriority() { return priority; }
    public GroupStatus getStatus() { return status; }
    public List<MemberPreviewResponse> getMemberPreview() { return memberPreview; }
    public int getMemberCount() { return memberCount; }
}