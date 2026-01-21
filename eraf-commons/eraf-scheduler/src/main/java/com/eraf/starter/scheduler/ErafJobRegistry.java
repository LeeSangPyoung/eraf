package com.eraf.starter.scheduler;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ERAF 작업 레지스트리
 * 등록된 스케줄 작업 관리
 */
public class ErafJobRegistry {

    private final Map<String, ErafJobInfo> jobs = new ConcurrentHashMap<>();

    /**
     * 작업 등록
     */
    public void register(ErafJobInfo jobInfo) {
        jobs.put(jobInfo.getName(), jobInfo);
    }

    /**
     * 작업 조회
     */
    public Optional<ErafJobInfo> getJob(String name) {
        return Optional.ofNullable(jobs.get(name));
    }

    /**
     * 모든 작업 조회
     */
    public Collection<ErafJobInfo> getAllJobs() {
        return jobs.values();
    }

    /**
     * 그룹별 작업 조회
     */
    public Collection<ErafJobInfo> getJobsByGroup(String group) {
        return jobs.values().stream()
                .filter(job -> group.equals(job.getGroup()))
                .toList();
    }

    /**
     * 작업 상태 업데이트
     */
    public void updateStatus(String name, ErafJobInfo.JobStatus status) {
        ErafJobInfo job = jobs.get(name);
        if (job != null) {
            job.setStatus(status);
        }
    }

    /**
     * 작업 제거
     */
    public void unregister(String name) {
        jobs.remove(name);
    }

    /**
     * 작업 존재 여부
     */
    public boolean exists(String name) {
        return jobs.containsKey(name);
    }

    /**
     * 작업 수
     */
    public int count() {
        return jobs.size();
    }
}
