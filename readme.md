Job Queuing
-
A job queue with optional resource locking.

## Adding the dependency
```
<dependencies>
    <dependency>
        <groupId>com.frejdh.util</groupId>
        <artifactId>job-queuing</artifactId>
        <version>1.2.0</version>
    </dependency>
</dependencies>

<repositories> <!-- Required in order to resolve this package -->
    <repository>
        <id>mvn-lib-job-queuing</id>
        <url>https://raw.github.com/Frejdh/mvn-lib-job-queuing/releases/</url>
    </repository>
</repositories>
```

### Usage
Some examples.

#### Starting Job Queue in background
````java
Job job = new Job(new JobFunction(() -> {
    System.out.println("My job")
}), "Optional resource ID, can be used to avoid concurrency issues");

final JobQueue queue = new JobQueueBuilder().buildAndStart();
queue.add(job);
Thread.sleep(100)
System.out.println("Generated job ID: " + job.getJobId() + ", current job status: " + job.getStatus());
````

#### Starting a Job Queue that should run only once (and await)
````java
List<Job> listOfJobs = Collections.singletonList(new Job(new JobFunction(() -> System.out.println("My job"))));

final JobQueue queue = new JobQueueBuilder().runOnceOnly().setPredefinedJobs(listOfJobs).buildAndStart();
queue.stopAndAwait(2000, TimeUnit.SECONDS);  // Stop with timeout option
````

## Other libraries
[Search for my other public libraries here](https://github.com/search?q=Frejdh%2Fmvn-lib-).
