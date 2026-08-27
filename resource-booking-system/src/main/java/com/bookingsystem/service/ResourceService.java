package com.bookingsystem.service;

import com.bookingsystem.dto.resource.ResourceRequest;
import com.bookingsystem.dto.resource.ResourceResponse;
import com.bookingsystem.exception.ResourceNotFoundException;
import com.bookingsystem.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;

    @Transactional(readOnly = true)
    public Page<ResourceResponse> list(String type, Boolean available, Pageable pageable) {
        Specification<com.bookingsystem.entity.Resource> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (type != null && !type.isBlank()) {
                predicate = cb.and(predicate, cb.equal(cb.upper(root.get("type")), type.toUpperCase()));
            }
            if (available != null) {
                predicate = cb.and(predicate, cb.equal(root.get("available"), available));
            }
            return predicate;
        };

        return resourceRepository.findAll(spec, pageable).map(ResourceResponse::from);
    }

    @Transactional(readOnly = true)
    public ResourceResponse getById(Long id) {
        return ResourceResponse.from(findEntity(id));
    }

    @Transactional
    public ResourceResponse create(ResourceRequest request) {
        var resource = com.bookingsystem.entity.Resource.builder()
                .name(request.name())
                .type(request.type())
                .description(request.description())
                .location(request.location())
                .capacity(request.capacity())
                .available(request.available() == null || request.available())
                .build();
        return ResourceResponse.from(resourceRepository.save(resource));
    }

    @Transactional
    public ResourceResponse update(Long id, ResourceRequest request) {
        var resource = findEntity(id);
        resource.setName(request.name());
        resource.setType(request.type());
        resource.setDescription(request.description());
        resource.setLocation(request.location());
        resource.setCapacity(request.capacity());
        if (request.available() != null) {
            resource.setAvailable(request.available());
        }
        return ResourceResponse.from(resourceRepository.save(resource));
    }

    @Transactional
    public void delete(Long id) {
        var resource = findEntity(id);
        resourceRepository.delete(resource);
    }

    private com.bookingsystem.entity.Resource findEntity(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
    }
}
