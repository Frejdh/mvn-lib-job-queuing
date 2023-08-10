[![Current Build](https://github.com/Frejdh/mvn-lib-job-queuing/actions/workflows/current-build.yml/badge.svg?branch=master)](https://github.com/Frejdh/mvn-lib-job-queuing/actions/workflows/current-build.yml)

Job Queuing
-
A multi-threaded job queue with optional resource locking.

## Adding the dependency
```
<dependencies>
    <dependency>
        <groupId>com.frejdh.util</groupId>
        <artifactId>job-queuing</artifactId>
        <version>1.3.0</version>
    </dependency>
</dependencies>

<repositories> <!-- Required in order to resolve this package -->
    <repository>
        <id>mvn-lib-job-queuing</id>
        <url>https://raw.github.com/Frejdh/mvn-lib-job-queuing/releases/</url>
    </repository>
</repositories>
```

## Usage
Some examples.

### Getting the JobQueue instance
There are two ways of creating a builder for the `JobQueue` class.
1. Retrieve the builder through a static method `'JobQueue.getBuilder()`
2. Create it manually `new JobQueueBuilder()`

### Starting Job Queue in background
```java
Job job = new Job(new JobFunction((jobRef) -> {
    System.out.println("My job")
}), "Optional resource ID, can be used to avoid concurrency issues");

final JobQueue queue = new JobQueueBuilder().buildAndStart();
queue.add(job);
System.out.println("Generated job ID: " + job.getJobId() + ", current job status: " + job.getStatus());
```

#### Starting a Job Queue that should run only once (and await)
```java
List<Job> listOfJobs = Collections.singletonList(new Job(new JobFunction((jobRef) -> System.out.println("My job"))));

final JobQueue queue = new JobQueueBuilder().runOnceOnly().setPredefinedJobs(listOfJobs).buildAndStart();
queue.stopAndAwait(2000, TimeUnit.SECONDS);  // Stop with timeout option
```

## Other libraries
[Search for my other public libraries here](https://github.com/search?q=Frejdh%2Fmvn-lib-).
