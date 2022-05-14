Changelog
-
For every release version.

# 1.3.0
- Job ID can now be set post the job build. The ID must be set however before the job is considered ready to be started.
  - New job state `WAITING_FOR_ID` has been added where the job is waiting for an ID to be set.
  - Note: This state only occurs when the
    `setJobIdAfterBuild` method is used with the builder, and during the time that the job ID isn't set, it'll then
    move to the `INITIALIZED` state as usual.
  - Note: The job in question isn't persisted when having the `WAITING_FOR_ID` state.
- Job now has a reference to itself in the `onAction` interface.
- Removed unused state `ADDED_TO_QUEUE`, and added state `CREATED` as a default value. `Job#getStatus()` is now never null.
- Better exception handling for potential thread pool errors.
- Code refactoring for DAO services.
- Custom JobQueue DAO implementations can now be provided in the Job Queue builder.
  - This also helped to fix the tests for JDK 17.

# 1.2.2
- Reordered logic so that job errors (throwables) are set before updating the status. 
  This is useful in the event that the `onStatusChange` method wants to do something that requires the error.

# 1.2.1
- Added the ability to set custom job IDs in case these should be tracked outside the queue implementation.

# 1.2.0
- Multiple callbacks support for `onCallback, onError, onFinalize & onStatusChange`.
  These can now also be added to already created jobs for easier post-creation handling.
- Refactored and removed old code.

# 1.1.1
- Job queue now uses a cached thread pool solution per default. This is configurable in the job queue builder.
- Removed unused option 'max amount of parallel jobs'.

# 1.1.0
- Stoppable jobs.
- Configurations per job-basis.
  - Timeout option is currently the only added one (so far).
- Persistence configurations available for jobs.
    - Optional H2 file implementation added, however the storage logic is not yet implemented!
- Moved to strict builder usage for classes (`Job` / `JobFunction`, etc).



# 1.0.0
First version
