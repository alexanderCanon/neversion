# Backend Testing Audit Prompt

The goal of this session is to understand the current testing health of the backend, not to write or modify any code. Think of yourself as an auditor doing a first pass.

The areas of most interest are the use cases (application services) and the output adapters (JPA repositories, external integrations), as these carry the most business risk. Existing unit and integration tests are present but their quality and coverage are unknown — JaCoCo has not been configured yet.

A good outcome for this session would be a markdown report that gives a clear picture of what exists, what's missing, and where the highest-risk gaps are. The report should be honest and prioritized, not exhaustive.
