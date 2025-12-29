package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;
import static org.springframework.util.StringUtils.hasText;

public class TodoCustomRepositoryImpl implements TodoCustomRepository {

    private final JPAQueryFactory queryFactory;

    public TodoCustomRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {

        return Optional.ofNullable(
                queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user)
                .fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne());
    }

    @Override
    public Page<TodoSearchResponse> searchTodos(String title, String managerName, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();

        if (hasText(title)) {
            builder.and(todo.title.containsIgnoreCase(title));
        }

        if (startDate != null) {
            builder.and(todo.createdAt.goe(startDate));
        }

        if (endDate != null) {
            builder.and(todo.createdAt.loe(endDate));
        }

        if (hasText(managerName)) {
            builder.and(
                    JPAExpressions.selectOne().from(manager).join(manager.user, user)
                            .where(manager.todo.eq(todo), user.nickname.containsIgnoreCase(managerName))
                            .exists()
            );
        }

        List<TodoSearchResponse> content = queryFactory
                .select(Projections.constructor(
                        TodoSearchResponse.class,
                        todo.title,

                        // 담당자 수
                        JPAExpressions
                                .select(manager.count())
                                .from(manager)
                                .where(manager.todo.eq(todo)),

                        // 댓글 수
                        JPAExpressions
                                .select(comment.count())
                                .from(comment)
                                .where(comment.todo.eq(todo))
                ))
                .from(todo)
                .where(builder)
                .orderBy(todo.createdAt.desc()) // 최신순 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(todo.count())
                .from(todo)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
