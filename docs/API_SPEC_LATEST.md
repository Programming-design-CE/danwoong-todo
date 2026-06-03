# Danwoong Todo API Spec

기준일: 2026-06-03

이 문서는 `src/main/java/com/danwoog/todo/controller` 와 현재 DTO 기준의 최신 API 명세입니다.

## 먼저 확인할 변경점

- 친구 API에 `GET /friends/requests/sent` 가 추가되었습니다.
- 프로젝트 멤버 추가는 `POST /todo-groups/{groupId}/invitations` 로 즉시 멤버를 추가합니다.
  - 이름은 `invitations` 이지만 실제 동작은 "초대 생성"이 아니라 "즉시 멤버 추가"입니다.
- 아래 API는 현재 구현되어 있지 않습니다.
  - `GET /todo-groups/invitations`
  - `PATCH /todo-groups/invitations/{invitationId}/accept`
  - `PATCH /todo-groups/invitations/{invitationId}/reject`
- 프로젝트 멤버 제거 API는 현재 `DELETE /todo-groups/{groupId}/members` 입니다.
- 파일 API의 루트 폴더는 더 이상 `"기본 폴더"` 고정이 아니라 프로젝트명 기준으로 생성/동기화됩니다.
- 메인 메모 수정은 `PUT /main/memo/{memoId}` 입니다.
- `shop`, `closet`, `file` API 는 `ApiResponse` 래퍼를 사용합니다.
  - 형식: `{ "success": true, "data": ..., "message": null }`
- `shop`, `closet` 컨트롤러는 현재 인증 사용자 대신 임시 사용자 ID `1`을 사용하고 있습니다.

---

## 1. User API

| Method | Endpoint | Request | Response |
| --- | --- | --- | --- |
| POST | `/users/signup` | `login_id`, `password`, `nickname` | `201 Created`, body 없음 |
| POST | `/users/login` | `login_id`, `password` | `access_token`, `refresh_token` |
| GET | `/users` | - | `user_id`, `nickname`, `garlic_count`, `character_thumbnail_url` |
| GET | `/users/character` | - | `character_id`, `user_id`, `equipped_items` |
| PATCH | `/users` | `nickname` | `user_id`, `nickname` |
| PATCH | `/users/garlic` | `amount` | `user_id`, `nickname`, `garlic_count`, `character_thumbnail_url` |

---

## 2. Friend API

| Method | Endpoint | Request | Response |
| --- | --- | --- | --- |
| POST | `/friends/requests` | `receiver_id` | `request_id`, `status` |
| GET | `/friends` | - | `friends: [{ user_id, nickname, character_thumbnail_url }]` |
| GET | `/friends/requests` | - | `requests: [{ request_id, sender_id, nickname, status }]` |
| GET | `/friends/requests/sent` | - | `requests: [{ request_id, receiver_id, nickname, status }]` |
| PATCH | `/friends/requests/{requestId}/accept` | - | `request_id`, `status` |
| PATCH | `/friends/requests/{requestId}/reject` | - | `request_id`, `status` |
| GET | `/friends/search?keyword=...` | query `keyword` | `users: [{ user_id, nickname, character_thumbnail_url }]` |

---

## 3. Todo Group API

### 3-1. 그룹 기본

| Method | Endpoint | Request | Response |
| --- | --- | --- | --- |
| POST | `/todo-groups` | `group_name`, `group_icon_url`, `group_category`, `deadline`, `priority`, `invitee_ids`, `total_garlic_reward` | `group_id`, `group_name`, `group_color`, `group_category`, `deadline`, `priority`, `status`, `total_garlic_reward`, `remaining_garlic_reward`, `members`, `member_count` |
| GET | `/todo-groups` | - | `groups: [{ group_id, group_name, group_color, group_category, deadline, priority, status, total_garlic_reward, remaining_garlic_reward, members, member_count, total_todo_count, completed_todo_count, leader_id }]` |
| PATCH | `/todo-groups/{groupId}` | `group_id`, `group_name`, `group_icon_url`, `group_category`, `deadline`, `priority`, `status` | `group_id`, `group_name`, `group_color`, `group_category`, `deadline`, `priority`, `status`, `total_garlic_reward`, `remaining_garlic_reward` |
| DELETE | `/todo-groups/{groupId}` | - | `group_id`, `status` |

### 3-2. 멤버 관리

| Method | Endpoint | Request | Response | 비고 |
| --- | --- | --- | --- | --- |
| POST | `/todo-groups/{groupId}/invitations` | `member_ids` | `group_id`, `invited_member_count` | 실제로는 "초대 생성"이 아니라 즉시 멤버 추가 |
| DELETE | `/todo-groups/{groupId}/members` | `member_ids` | `group_id`, `removed_member_count` | 리더만 제거 가능, 리더/본인 제거 방지 |

### 3-3. 마늘 분배

| Method | Endpoint | Request | Response |
| --- | --- | --- | --- |
| POST | `/todo-groups/{groupId}/garlic-distribution` | `distributions: [{ user_id, reward_amount }]` | body 없음 |

---

## 4. Group Todo API

| Method | Endpoint | Request | Response |
| --- | --- | --- | --- |
| POST | `/todo-groups/{groupId}/todos` | `todo_name`, `description`, `deadline`, `garlic_reward`, `priority`, `category`, `distribution_type`, `assignees: [{ user_id, reward_amount }]` | `todo_id`, `distribution_type`, `assignees: [{ user_id, nickname, reward_amount }]` |
| GET | `/todo-groups/{groupId}/todos` | query `status` optional | `todos: [{ todo_id, todo_name, deadline, priority, status, distribution_type, garlic_reward, assignees }]` |
| GET | `/todos/{todoId}` | - | `todo_id`, `todo_name`, `description`, `deadline`, `priority`, `status`, `garlic_reward`, `category`, `distribution_type`, `assignees` |
| PATCH | `/todos/{todoId}` | `todo_name`, `description`, `deadline`, `priority`, `category`, `status`, `garlic_reward`, `distribution_type`, `assignees` | `todo_id` |
| PATCH | `/todos/{todoId}/complete` | - | `todo_id`, `status`, `garlic_reward`, `rewarded_assignees` |
| DELETE | `/todos/{todoId}` | - | `204 No Content` |
| GET | `/todo-groups/{groupId}/note` | - | `group_note_id`, `content` |
| PUT | `/todo-groups/{groupId}/note` | `content` | `group_note_id`, `content` |

---

## 5. Personal Todo API

| Method | Endpoint | Request | Response |
| --- | --- | --- | --- |
| GET | `/todos/my` | - | `todos: [{ todo_id, todo_name, group_name, deadline, garlic_reward, priority }]` |
| GET | `/todos/my/completed` | - | `todos: [{ todo_id, todo_name, completed_at, garlic_reward, assignee_nicknames }]` |
| GET | `/todos/my/note` | - | `content` |
| PUT | `/todos/my/note` | `content` | `200 OK`, body 없음 |
| GET | `/todos/my/statistics` | - | `progress_rate`, `expected_garlic`, `category_summary: [{ category, total_count, completed_count }]` |

---

## 6. Shop API

응답은 모두 `ApiResponse` 래퍼를 사용합니다.

| Method | Endpoint | Request | `data` 필드 |
| --- | --- | --- | --- |
| GET | `/shop/garlic` | - | `garlicCount` |
| GET | `/shop/items` | - | `[{ itemId, itemName, itemType, itemImage, price }]` |
| POST | `/shop/items/{itemId}/purchase` | `count` | `purchaseId`, `itemId`, `remainingGarlic` |

---

## 7. Closet API

응답은 모두 `ApiResponse` 래퍼를 사용합니다.

| Method | Endpoint | Request | `data` 필드 |
| --- | --- | --- | --- |
| GET | `/closet/items` | - | `[{ itemId, itemName, itemType, quantity }]` |
| PATCH | `/closet/equipped-items` | `itemId`, `slotType` | `characterId`, `characterThumbnailUrl` |
| POST | `/closet/items/{itemId}/use` | - | `itemId`, `quantity` |

---

## 8. Main Memo API

| Method | Endpoint | Request | Response |
| --- | --- | --- | --- |
| GET | `/main/memo` | - | `memos: [{ memo_id, content }]` |
| POST | `/main/memo` | `content` | `memo_id`, `content` |
| PUT | `/main/memo/{memoId}` | `content` | `memo_id`, `content` |
| DELETE | `/main/memo/{memoId}` | - | `204 No Content` |

---

## 9. File API

응답은 조회/생성/삭제 API 대부분 `ApiResponse` 래퍼를 사용합니다.

### 래퍼 예시

```json
{
  "success": true,
  "data": {},
  "message": null
}
```

| Method | Endpoint | Request | `data` 필드 |
| --- | --- | --- | --- |
| GET | `/todo-groups/{groupId}/folders/root` | - | `folderId`, `folderName`, `parentFolderId`, `createdAt`, `totalSize` |
| POST | `/todo-groups/{groupId}/folders/{folderId}/folders` | `folderName` | `folderId`, `folderName`, `parentFolderId`, `createdAt`, `totalSize` |
| GET | `/todo-groups/{groupId}/folders/{folderId}/items` | - | `currentFolder`, `folders`, `files` |
| POST | `/todo-groups/{groupId}/folders/{folderId}/files` | `multipart/form-data` with `file` | `fileId`, `originalName`, `fileUrl`, `fileSize`, `fileType`, `uploadedAt` |
| GET | `/files/{fileId}` | - | 파일 리소스 |
| DELETE | `/files/{fileId}` | - | `data: null` |
| DELETE | `/folders/{folderId}` | - | `data: null` |

`FolderItemsResponse` 상세 구조:

- `currentFolder`
  - `folderId`, `folderName`, `parentFolderId`, `createdAt`, `totalSize`
- `folders`
  - `FolderResponse[]`
- `files`
  - `FileResponse[]`

---

## 10. Calendar API

| Method | Endpoint | Request | Response |
| --- | --- | --- | --- |
| GET | `/calendar/todos?date=YYYY-MM-DD` | query `date` | `date`, `count`, `todos: [{ todoId, title, date, isCompleted, group_name, category, priority }]` |
| GET | `/calendar/month?year=YYYY&month=M` | query `year`, `month` | `year`, `month`, `days: [{ date, count, todos }]` |

---

## 11. old 명세와 다른 핵심 포인트 요약

1. 친구 API
- `GET /friends/requests/sent` 가 추가되었습니다.

2. 그룹 초대/멤버
- old 명세의 "그룹 초대 목록 / 수락 / 거절" 3개 API는 실제 구현되어 있지 않습니다.
- 현재 `POST /todo-groups/{groupId}/invitations` 는 즉시 멤버 추가 API 입니다.
- 현재 `DELETE /todo-groups/{groupId}/members` 로 멤버 제거가 가능합니다.

3. 메인 메모
- 수정 API는 `PUT /main/memo/{memoId}` 입니다.

4. 파일 API
- 루트 폴더 이름은 프로젝트명 기준입니다.
- `GET /todo-groups/{groupId}/folders/{folderId}/items` 응답은 old 명세처럼 단일 `items` 배열이 아니라 `currentFolder`, `folders`, `files` 로 나뉩니다.
- 폴더 응답에 `totalSize` 가 포함됩니다.

5. Personal Todo
- `GET /todos/my` 는 old 명세처럼 `status` 쿼리를 받지 않습니다.
- `PUT /todos/my/note` 는 body 없는 `200 OK` 를 반환합니다.

6. Shop / Closet / File
- old 명세에는 직접 DTO만 적혀 있었지만 실제 응답은 `ApiResponse` 래퍼 안에 들어갑니다.
