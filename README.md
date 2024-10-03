## Meetups Calendar

Calendar for developers meetups, live at https://meetups.dev

### How to add your own meetup?

Easy! Just send me a PR, adding your meetup id (for example, in https://www.meetup.com/montreal-jug/events/ `montreal-jug` is the meetup id) to this [src/main/resources/application.yml](https://github.com/anthonydahanne/meetups-calendar/blob/main/src/main/resources/application.yml)

```yaml
groups:
  meetup:
    montreal:
      - my-meetup-id
```


